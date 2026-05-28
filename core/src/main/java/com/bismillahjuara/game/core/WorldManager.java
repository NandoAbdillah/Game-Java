package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
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
    // AAA OPTIMIZATION: SPATIAL PARTITIONING (SISTEM AREA/CHUNK)
    // ==========================================================
    // Membagi dunia menjadi petak-petak (Area) berukuran 25x25 meter
    private static final float CHUNK_SIZE = 25f;

    // LongMap sangat ramah RAM HP (Zero GC). Menyimpan daftar pohon per-Area.
    private LongMap<Array<BoundingBox>> spatialChunks;

    // Array ini HANYA dipakai untuk menggambar garis merah saat mode debug nyala
    private Array<BoundingBox> allCollisionsForDebug;

    private int collisionCount = 0;
    private Vector3 tempCenter = new Vector3(); // Reuse vector untuk mencegah memori bocor

    private ShapeRenderer debugRenderer;
    public boolean isDebugMode = false; // Biarkan false agar enteng saat dimainkan

    public WorldManager(GameContext context) {
        this.context = context;
        this.spatialChunks = new LongMap<>();
        this.allCollisionsForDebug = new Array<>();
    }

    public void initialize(SceneRenderer renderer) {
        try {
            debugRenderer = new ShapeRenderer();

            mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Maps2.glb"));
            mapScene = new Scene(mapAsset.scene);

            mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
            mapScene.modelInstance.calculateTransforms();

            collisionCount = 0;

            Gdx.app.log("WORLD", "Memulai Ekstraksi & Pemetaan Area Collision...");
            extractAndHideCollisions(mapScene.modelInstance.nodes);

            renderer.addScene(mapScene);
            Gdx.app.log("WORLD", "Map berhasil diload! TOTAL TEMBOK GAIB: " + collisionCount + " (Telah dioptimasi ke dalam Area)");

        } catch (Exception e) {
            Gdx.app.log("WORLD_WARNING", "Map.glb tidak ditemukan, abaikan jika masih dummy.", e);
        }
    }

    /**
     * Membuat ID Unik untuk setiap petak tanah (Chunk).
     * Sangat cepat karena menggunakan operasi Bitwise (Shift).
     */
    private long getChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xffffffffL);
    }

    private void extractAndHideCollisions(Iterable<Node> nodes) {
        for (Node node : nodes) {
            String nodeName = node.id != null ? node.id.toLowerCase() : "unnamed";

            if (nodeName.contains("pohon_col") || nodeName.contains("collider")) {

                BoundingBox box = new BoundingBox();
                node.calculateBoundingBox(box, true);

                if (box.isValid()) {
                    box.mul(mapScene.modelInstance.transform);

                    // --- MASUKKAN KOTAK KE DALAM AREA (CHUNK) YANG TEPAT ---
                    box.getCenter(tempCenter);
                    int chunkX = MathUtils.floor(tempCenter.x / CHUNK_SIZE);
                    int chunkZ = MathUtils.floor(tempCenter.z / CHUNK_SIZE);
                    long key = getChunkKey(chunkX, chunkZ);

                    Array<BoundingBox> chunk = spatialChunks.get(key);
                    if (chunk == null) {
                        chunk = new Array<>();
                        spatialChunks.put(key, chunk);
                    }

                    chunk.add(box); // Masukkan ke area spesifik
                    allCollisionsForDebug.add(box); // Masukkan ke data debug
                    collisionCount++;
                }

                hideNodeAndChildren(node);

            } else if (node.hasChildren()) {
                extractAndHideCollisions(node.getChildren());
            }
        }
    }

    private void hideNodeAndChildren(Node node) {
        node.parts.clear();

        node.localTransform.setToScaling(0f, 0f, 0f);
        node.globalTransform.setToScaling(0f, 0f, 0f);
        node.localTransform.setTranslation(0f, -9999f, 0f);
        node.globalTransform.setTranslation(0f, -9999f, 0f);
        node.isAnimated = false;

        if (node.hasChildren()) {
            for (Node child : node.getChildren()) {
                hideNodeAndChildren(child);
            }
        }
    }

    public boolean isColliding(Vector3 newPosition, float entityRadius, float entityHeight) {
        BoundingBox entityBox = new BoundingBox(
            new Vector3(newPosition.x - entityRadius, newPosition.y, newPosition.z - entityRadius),
            new Vector3(newPosition.x + entityRadius, newPosition.y + entityHeight, newPosition.z + entityRadius)
        );

        // 1. Cari tahu Player sedang berada di Area (Chunk) mana?
        int playerChunkX = MathUtils.floor(newPosition.x / CHUNK_SIZE);
        int playerChunkZ = MathUtils.floor(newPosition.z / CHUNK_SIZE);

        // 2. Cek Area tempat Player berdiri, PLUS 8 area di sekelilingnya (Total 9 petak terdekat)
        // Hal ini untuk mencegah tembus tembok jika player berdiri tepat di garis batas area.
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {

                long key = getChunkKey(playerChunkX + offsetX, playerChunkZ + offsetZ);
                Array<BoundingBox> localTrees = spatialChunks.get(key);

                // Jika di area ini ada pohon, maka cek tabrakannya!
                if (localTrees != null) {
                    // Menggunakan For-Loop konvensional (Bukan foreach) agar RAM HP tidak bocor (Zero Garbage)
                    for (int i = 0; i < localTrees.size; i++) {
                        if (localTrees.get(i).intersects(entityBox)) {
                            return true; // Mentok!
                        }
                    }
                }
            }
        }

        return false; // Aman!
    }

    public void update(float fixedDelta) {}

    public void renderDebug(PerspectiveCamera cam) {
        if (!isDebugMode || debugRenderer == null || allCollisionsForDebug.size == 0) return;

        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.RED);

        for (BoundingBox box : allCollisionsForDebug) {
            debugRenderer.box(
                box.min.x, box.min.y, box.min.z,
                box.getWidth(), box.getHeight(), box.getDepth()
            );
        }

        debugRenderer.end();
    }

    public void dispose() {
        if (mapAsset != null) mapAsset.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        spatialChunks.clear();
        allCollisionsForDebug.clear();
    }
}
