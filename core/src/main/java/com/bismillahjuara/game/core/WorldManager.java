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

    // ==========================================================
    // AAA SMART RENDERING & HYBRID CULLING SYSTEM
    // ==========================================================
    private static final float CHUNK_SIZE = 30f;
    private static final float SHOW_DISTANCE = 75f; // Jarak objek mulai muncul
    private static final float HIDE_DISTANCE = 90f; // Jarak objek mulai hilang (Hysteresis)

    // Data Entity Visual (Zero Mutation, Cache Friendly)
    private static class VisibilityObject {
        Array<NodePart> parts; // Referensi langsung ke parts untuk bypass perulangan scene graph
        Vector3 center;
        float radius;
        boolean isVisible;

        VisibilityObject(Node node, Vector3 center, float radius) {
            this.parts = node.parts;
            this.center = new Vector3(center);
            this.radius = radius;
            this.isVisible = true;
        }

        // Murni hanya mematikan flag Render Pipeline LibGDX, TIDAK merusak Scene Graph!
        void setVisible(boolean visible) {
            if (this.isVisible == visible) return;
            this.isVisible = visible;
            for (int i = 0; i < parts.size; i++) {
                parts.get(i).enabled = visible;
            }
        }
    }

    // Data Area Spasial
    private static class VisibilityChunk {
        Vector3 center;
        float radius;
        Array<VisibilityObject> objects = new Array<>(false, 64);
        Array<BoundingBox> collisions = new Array<>(false, 16);
        boolean isActive = false;

        VisibilityChunk(Vector3 center) {
            this.center = new Vector3(center);
            // Radius chunk (Setengah diagonal kotak)
            this.radius = (CHUNK_SIZE * 1.414f) / 2f;
        }
    }

    private LongMap<VisibilityChunk> spatialHash;
    private Array<VisibilityChunk> allChunks; // Flat array untuk iterasi Zero GC
    private Array<VisibilityChunk> activeChunks;
    private Array<VisibilityObject> largeObjects; // Terrain / Gunung

    // Stats & Debug
    private int collisionCount = 0;
    private int visualCount = 0;
    private int largeObjectCount = 0;

    private ShapeRenderer debugRenderer;
    public boolean isDebugMode = false;

    // Cache Vector untuk kalkulasi mencegah GC Spike
    private Vector3 tempCenter = new Vector3();
    private Vector3 tempDim = new Vector3();

    public WorldManager(GameContext context) {
        this.context = context;
        this.spatialHash = new LongMap<>();
        this.allChunks = new Array<>(false, 128);
        this.activeChunks = new Array<>(false, 64);
        this.largeObjects = new Array<>(false, 16);
    }

    public void initialize(SceneRenderer renderer) {
        try {
            if (isDebugMode) debugRenderer = new ShapeRenderer();

            mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Maps2.glb"));
            mapScene = new Scene(mapAsset.scene);

            mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
            mapScene.modelInstance.calculateTransforms();

            Gdx.app.log("WORLD", "Memulai Ekstraksi Pipeline Bounding Sphere...");
            scanAndRegisterNodes(mapScene.modelInstance.nodes);

            renderer.addScene(mapScene);

            // Initial hide: Matikan semua objek di awal sebelum update pertama
            for (VisibilityChunk chunk : allChunks) {
                for (VisibilityObject vo : chunk.objects) vo.setVisible(false);
            }

            Gdx.app.log("WORLD", String.format("Map Selesai! Collisions: %d | Visuals: %d | Large Terrains: %d",
                collisionCount, visualCount, largeObjectCount));

        } catch (Exception e) {
            Gdx.app.log("WORLD_WARNING", "Map.glb gagal di-load.", e);
        }
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

    private void scanAndRegisterNodes(Iterable<Node> nodes) {
        BoundingBox box = new BoundingBox();

        for (Node node : nodes) {
            String nodeName = node.id != null ? node.id.toLowerCase() : "unnamed";

            // 1. COLLISION SYSTEM (Tetap hidden selamanya)
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

                // Render Filtering: Set disabled agar ModelBatch melewatinya. Node tidak di-clear.
                for (NodePart part : node.parts) part.enabled = false;
                node.isAnimated = false;
            }
            // 2. VISUAL SYSTEM (Rumput, Pohon, Tanah)
            else if (node.parts.size > 0) {
                box.inf();
                node.calculateBoundingBox(box, true);

                if (box.isValid()) {
                    box.mul(mapScene.modelInstance.transform);
                    box.getCenter(tempCenter);
                    box.getDimensions(tempDim);

                    // Gunakan Bounding Sphere Radius agar Frustum Culling sangat cepat
                    float radius = tempDim.len() / 2f;
                    float maxDim = Math.max(tempDim.x, Math.max(tempDim.y, tempDim.z));

                    VisibilityObject vo = new VisibilityObject(node, tempCenter, radius);

                    // STRATEGI OBJEK RAKSASA: Jika ukuran > 40m (Terrain), jangan di-distance cull!
                    if (maxDim > 40f) {
                        largeObjects.add(vo);
                        largeObjectCount++;
                        vo.setVisible(true); // Selalu ter-submit ke ModelBatch, biarkan GL natively frustum cull
                    } else {
                        int cX = MathUtils.floor(tempCenter.x / CHUNK_SIZE);
                        int cZ = MathUtils.floor(tempCenter.z / CHUNK_SIZE);
                        getOrCreateChunk(cX, cZ).objects.add(vo);
                        visualCount++;
                    }
                }
            }

            if (node.hasChildren()) {
                scanAndRegisterNodes(node.getChildren());
            }
        }
    }

    // UPDATE RENDER PIPELINE: Dipanggil setiap frame sebelum render
    public void update(float fixedDelta) {
        if (context == null || context.camera == null) return;

        PerspectiveCamera cam = context.camera.getCam();
        Vector3 camPos = cam.position;

        // 1. HYSTERESIS CHUNK ACTIVATION (Distance Culling)
        // Kita iterasi semua chunk menggunakan Flat Array agar Zero GC!
        for (int i = 0; i < allChunks.size; i++) {
            VisibilityChunk chunk = allChunks.get(i);

            // Jarak Kamera ke pusat Chunk (Pakai distance kuadrat agar lebih cepat, tapi dst biasa juga cukup untuk CPU modern)
            float dist = chunk.center.dst(camPos);

            if (!chunk.isActive && dist <= SHOW_DISTANCE) {
                chunk.isActive = true;
                activeChunks.add(chunk);
            }
            else if (chunk.isActive && dist > HIDE_DISTANCE) {
                chunk.isActive = false;
                activeChunks.removeValue(chunk, true);

                // Matikan semua objek di chunk yang ditinggalkan
                for (int j = 0; j < chunk.objects.size; j++) {
                    chunk.objects.get(j).setVisible(false);
                }
            }
        }

        // 2. FRUSTUM CULLING PADA CHUNK YANG AKTIF SAJA
        for (int i = 0; i < activeChunks.size; i++) {
            VisibilityChunk chunk = activeChunks.get(i);

            // OPTIMASI AAA: Cek apakah seluruh Chunk ini ada di belakang kamera?
            if (!cam.frustum.sphereInFrustum(chunk.center, chunk.radius)) {
                // Chunk di luar layar, matikan semua objeknya tanpa cek detail!
                for (int j = 0; j < chunk.objects.size; j++) {
                    chunk.objects.get(j).setVisible(false);
                }
                continue; // Lanjut ke chunk berikutnya
            }

            // Jika Chunk terlihat, lakukan Frustum Check detail per-objek (Bounding Sphere)
            for (int j = 0; j < chunk.objects.size; j++) {
                VisibilityObject vo = chunk.objects.get(j);
                boolean inFrustum = cam.frustum.sphereInFrustum(vo.center, vo.radius);
                vo.setVisible(inFrustum);
            }
        }
    }

    // LOGIKA COLLISION (Sudah dioptimasi dengan Spatial Hashmap)
    public boolean isColliding(Vector3 newPosition, float entityRadius, float entityHeight) {
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
        debugRenderer.setColor(Color.RED);

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
    }
}
