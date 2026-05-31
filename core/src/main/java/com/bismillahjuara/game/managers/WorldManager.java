package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.core.SceneRenderer;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class WorldManager {

    private GameContext context;
    private SceneAsset mapAsset;
    private Scene mapScene;
    private float mapScale = 10f;

    public BoundingBox mapBounds;
    public float safePlayMargin = 15f;

    public Vector3 playerSpawnPos = new Vector3();
    public Array<Vector3> enemySpawnPositions = new Array<>(false, 10);

    private static final float CHUNK_SIZE = 30f;
    private static final float SHOW_DISTANCE = 75f;
    private static final float HIDE_DISTANCE = 90f;

    private static class VisibilityObject {
        Array<NodePart> parts;
        Vector3 center;
        float radius;
        boolean isVisible;

        VisibilityObject(Node node, Vector3 center, float radius) {
            this.parts = node.parts;
            this.center = new Vector3(center);
            this.radius = radius;
            this.isVisible = true;
        }

        void setVisible(boolean visible) {
            if (this.isVisible == visible) return;
            this.isVisible = visible;
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).enabled = visible;
            }
        }
    }

    private static class VisibilityChunk {
        Vector3 center;
        float radius;
        Array<VisibilityObject> objects = new Array<>(false, 64);
        Array<BoundingBox> collisions = new Array<>(false, 16);
        boolean isActive = false;

        VisibilityChunk(Vector3 center) {
            this.center = new Vector3(center);
            this.radius = (CHUNK_SIZE * 1.414f) / 2f;
        }
    }

    private LongMap<VisibilityChunk> spatialHash;
    private Array<VisibilityChunk> allChunks;
    private Array<VisibilityChunk> activeChunks;
    private Array<VisibilityObject> largeObjects;

    private int collisionCount = 0;
    private int visualCount = 0;
    private int largeObjectCount = 0;

    private ShapeRenderer debugRenderer;
    public boolean isDebugMode = false;

    private Vector3 tempCenter = new Vector3();
    private Vector3 tempDim = new Vector3();

    // --- AAA TIME-SLICED PIPELINE VARIABLES ---
    private Array<Node> nodeQueue; // Antrean node yang belum diproses
    private int totalNodesToProcess = 0;

    public WorldManager(GameContext context) {
        this.context = context;
        this.spatialHash = new LongMap<>();
        this.allChunks = new Array<>(false, 128);
        this.activeChunks = new Array<>(false, 64);
        this.largeObjects = new Array<>(false, 16);
        this.mapBounds = new BoundingBox();
        this.nodeQueue = new Array<>(false, 1024);
    }

    // =========================================================================
    // TAHAP 1: BACA FILE FISIK DARI HARDDISK (I/O)
    // =========================================================================
    public void step1_LoadMapDisk() {
        long startTime = System.currentTimeMillis();
        mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Maps.glb"));
        Gdx.app.log("PROFILE_WORLD", "Load Map.glb dari Disk selesai: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    // =========================================================================
    // TAHAP 2: BANGUN SCENE DAN SIAPKAN ANTREAN NODE
    // =========================================================================
//    public void step2_BuildSceneAndQueue(SceneRenderer renderer) {
//        long startTime = System.currentTimeMillis();
//        if (isDebugMode) debugRenderer = new ShapeRenderer();
//
//        mapScene = new Scene(mapAsset.scene);
//        mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
//        mapScene.modelInstance.calculateTransforms();
//
//        mapScene.modelInstance.calculateBoundingBox(mapBounds);
//        mapBounds.mul(mapScene.modelInstance.transform);
//
//        renderer.addScene(mapScene);
//
//        // Jangan proses langsung! Ratakan hirarki tree (Flatten) dan masukkan ke Queue
//        flattenNodeTree(mapScene.modelInstance.nodes);
//        totalNodesToProcess = nodeQueue.size;
//
//        Gdx.app.log("PROFILE_WORLD", "Build Scene & Flatten " + totalNodesToProcess + " nodes selesai: " + (System.currentTimeMillis() - startTime) + " ms");
//    }

    public void step2_BuildSceneAndQueue(SceneRenderer renderer) {
        long startTime = System.currentTimeMillis();
        if (isDebugMode) debugRenderer = new ShapeRenderer();

        // MENGAMBIL ASET MATANG DARI CACHE (0 ms I/O)
        mapAsset = GameAssets.getInstance().manager.get(GameAssets.MAP_GLB, SceneAsset.class);

        mapScene = new Scene(mapAsset.scene);
        mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
        mapScene.modelInstance.calculateTransforms();

        mapScene.modelInstance.calculateBoundingBox(mapBounds);
        mapBounds.mul(mapScene.modelInstance.transform);

        renderer.addScene(mapScene);

        flattenNodeTree(mapScene.modelInstance.nodes);
        totalNodesToProcess = nodeQueue.size;

        Gdx.app.log("PROFILE_WORLD", "Build Scene 22k nodes selesai: " + (System.currentTimeMillis() - startTime) + " ms");
    }
    private void flattenNodeTree(Iterable<Node> nodes) {
        for (Node node : nodes) {
            nodeQueue.add(node);
            if (node.hasChildren()) {
                flattenNodeTree(node.getChildren());
            }
        }
    }

    // =========================================================================
    // TAHAP 3: PROSES NODE SECARA NYICIL (TIME-SLICED)
    // Return TRUE jika semua node sudah selesai diproses.
    // =========================================================================
    public boolean step3_ProcessNodesAsync() {
        long startTime = System.currentTimeMillis();
        int processedThisFrame = 0;
        BoundingBox box = new BoundingBox();

        // Batasi pemrosesan maksimal 15 milidetik per frame (Menjaga 60 FPS saat loading)
        while (nodeQueue.size > 0 && (System.currentTimeMillis() - startTime) < 15) {
            Node node = nodeQueue.pop(); // Ambil dari belakang (cepat)
            processSingleNode(node, box);
            processedThisFrame++;
        }

        // Jika antrean habis, sembunyikan semua secara default
        if (nodeQueue.size == 0) {
            for (VisibilityChunk chunk : allChunks) {
                for (VisibilityObject vo : chunk.objects) vo.setVisible(false);
            }
            Gdx.app.log("PROFILE_WORLD", "Pemindaian " + totalNodesToProcess + " Nodes TUNTAS. Collisions: " + collisionCount + ", Visuals: " + visualCount);
            return true;
        }

        return false; // Belum selesai, lanjut frame berikutnya
    }

    private void processSingleNode(Node node, BoundingBox box) {
        String nodeName = node.id != null ? node.id.toLowerCase() : "unnamed";

        if (nodeName.contains("pohon_col") || nodeName.contains("collider")) {
            box.inf();
            node.calculateBoundingBox(box, true);
            if (box.isValid()) {
                box.mul(mapScene.modelInstance.transform);
                box.getCenter(tempCenter);

                int cX = MathUtils.floor(tempCenter.x / CHUNK_SIZE);
                int cZ = MathUtils.floor(tempCenter.z / CHUNK_SIZE);
                getOrCreateChunk(cX, cZ).collisions.add(new BoundingBox(box));
                collisionCount++;
            }

            for (NodePart part : node.parts) part.enabled = false;
            node.isAnimated = false;
        }
        else if (node.parts.size > 0) {
            box.inf();
            node.calculateBoundingBox(box, true);

            if (box.isValid()) {
                box.mul(mapScene.modelInstance.transform);
                box.getCenter(tempCenter);
                box.getDimensions(tempDim);

                float radius = tempDim.len() / 2f;
                float maxDim = Math.max(tempDim.x, Math.max(tempDim.y, tempDim.z));

                VisibilityObject vo = new VisibilityObject(node, tempCenter, radius);

                if (maxDim > 40f) {
                    largeObjects.add(vo);
                    largeObjectCount++;
                    vo.setVisible(true);
                } else {
                    int cX = MathUtils.floor(tempCenter.x / CHUNK_SIZE);
                    int cZ = MathUtils.floor(tempCenter.z / CHUNK_SIZE);
                    getOrCreateChunk(cX, cZ).objects.add(vo);
                    visualCount++;
                }
            }
        }
    }

    public float getAsyncProgress() {
        if (totalNodesToProcess == 0) return 0f;
        return 1f - ((float)nodeQueue.size / totalNodesToProcess);
    }

    // --- HELPER UNTUK SPAWN PINTAR ---
    public void getSafeCenterPosition(Vector3 out) {
        float safeMinX = mapBounds.min.x + safePlayMargin;
        float safeMaxX = mapBounds.max.x - safePlayMargin;
        float safeMinZ = mapBounds.min.z + safePlayMargin;
        float safeMaxZ = mapBounds.max.z - safePlayMargin;
        out.set((safeMinX + safeMaxX) / 2f, 0, (safeMinZ + safeMaxZ) / 2f);
    }

    public void getRandomSafePosition(Vector3 out) {
        float safeMinX = mapBounds.min.x + safePlayMargin;
        float safeMaxX = mapBounds.max.x - safePlayMargin;
        float safeMinZ = mapBounds.min.z + safePlayMargin;
        float safeMaxZ = mapBounds.max.z - safePlayMargin;
        out.set(MathUtils.random(safeMinX, safeMaxX), 0, MathUtils.random(safeMinZ, safeMaxZ));
    }

    private long getChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    private VisibilityChunk getOrCreateChunk(int chunkX, int chunkZ) {
        long key = getChunkKey(chunkX, chunkZ);
        VisibilityChunk chunk = spatialHash.get(key);
        if (chunk == null) {
            Vector3 center = new Vector3((chunkX * CHUNK_SIZE) + (CHUNK_SIZE / 2f), 0, (chunkZ * CHUNK_SIZE) + (CHUNK_SIZE / 2f));
            chunk = new VisibilityChunk(center);
            spatialHash.put(key, chunk);
            allChunks.add(chunk);
        }
        return chunk;
    }

    public void update(float fixedDelta) {
        if (context == null || context.camera == null) return;
        PerspectiveCamera cam = context.camera.getCam();
        Vector3 camPos = cam.position;

        for (int i = 0; i < allChunks.size; i++) {
            VisibilityChunk chunk = allChunks.get(i);
            float dist = chunk.center.dst(camPos);

            if (!chunk.isActive && dist <= SHOW_DISTANCE) {
                chunk.isActive = true;
                activeChunks.add(chunk);
            }
            else if (chunk.isActive && dist > HIDE_DISTANCE) {
                chunk.isActive = false;
                activeChunks.removeValue(chunk, true);

                for (int j = 0; j < chunk.objects.size; j++) {
                    chunk.objects.get(j).setVisible(false);
                }
            }
        }

        for (int i = 0; i < activeChunks.size; i++) {
            VisibilityChunk chunk = activeChunks.get(i);

            if (!cam.frustum.sphereInFrustum(chunk.center, chunk.radius)) {
                for (int j = 0; j < chunk.objects.size; j++) {
                    chunk.objects.get(j).setVisible(false);
                }
                continue;
            }

            for (int j = 0; j < chunk.objects.size; j++) {
                VisibilityObject vo = chunk.objects.get(j);
                boolean inFrustum = cam.frustum.sphereInFrustum(vo.center, vo.radius);
                vo.setVisible(inFrustum);
            }
        }
    }

    public boolean isColliding(Vector3 newPosition, float entityRadius, float entityHeight) {
        float safeMinX = mapBounds.min.x + safePlayMargin;
        float safeMaxX = mapBounds.max.x - safePlayMargin;
        float safeMinZ = mapBounds.min.z + safePlayMargin;
        float safeMaxZ = mapBounds.max.z - safePlayMargin;

        if (newPosition.x - entityRadius < safeMinX || newPosition.x + entityRadius > safeMaxX ||
            newPosition.z - entityRadius < safeMinZ || newPosition.z + entityRadius > safeMaxZ) {
            return true;
        }

        BoundingBox entityBox = new BoundingBox(
            new Vector3(newPosition.x - entityRadius, newPosition.y, newPosition.z - entityRadius),
            new Vector3(newPosition.x + entityRadius, newPosition.y + entityHeight, newPosition.z + entityRadius)
        );

        int playerChunkX = MathUtils.floor(newPosition.x / CHUNK_SIZE);
        int playerChunkZ = MathUtils.floor(newPosition.z / CHUNK_SIZE);

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                long key = getChunkKey(playerChunkX + offsetX, playerChunkZ + offsetZ);
                VisibilityChunk chunk = spatialHash.get(key);
                if (chunk != null) {
                    for (int i = 0; i < chunk.collisions.size; i++) {
                        if (chunk.collisions.get(i).intersects(entityBox)) return true;
                    }
                }
            }
        }
        return false;
    }

    public void renderDebug(PerspectiveCamera cam) {
        if (!isDebugMode || debugRenderer == null) return;
        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        debugRenderer.setColor(Color.YELLOW);
        debugRenderer.box(mapBounds.min.x, 0, mapBounds.min.z, mapBounds.max.x - mapBounds.min.x, 50f, mapBounds.max.z - mapBounds.min.z);

        debugRenderer.setColor(Color.GREEN);
        float safeMinX = mapBounds.min.x + safePlayMargin;
        float safeMinZ = mapBounds.min.z + safePlayMargin;
        float safeWidth = (mapBounds.max.x - safePlayMargin) - safeMinX;
        float safeDepth = (mapBounds.max.z - safePlayMargin) - safeMinZ;
        debugRenderer.box(safeMinX, 0, safeMinZ, safeWidth, 50f, safeDepth);

        if (playerSpawnPos != null) {
            debugRenderer.setColor(Color.BLUE);
            debugRenderer.box(playerSpawnPos.x - 0.5f, 0, playerSpawnPos.z - 0.5f, 1f, 3f, 1f);
        }

        debugRenderer.setColor(Color.RED);
        for (Vector3 ePos : enemySpawnPositions) {
            debugRenderer.box(ePos.x - 0.5f, 0, ePos.z - 0.5f, 1f, 3f, 1f);
        }

        debugRenderer.setColor(new Color(0.5f, 0f, 0f, 1f));
        for (int i = 0; i < allChunks.size; i++) {
            Array<BoundingBox> colls = allChunks.get(i).collisions;
            for (int j = 0; j < colls.size; j++) {
                BoundingBox box = colls.get(j);
                debugRenderer.box(box.min.x, box.min.y, box.min.z, box.getWidth(), box.getHeight(), box.getDepth());
            }
        }
        debugRenderer.end();
    }

    public void dispose() {
        if (mapAsset != null) mapAsset.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        spatialHash.clear();
        allChunks.clear();
        activeChunks.clear();
        largeObjects.clear();
        nodeQueue.clear();
    }
}
