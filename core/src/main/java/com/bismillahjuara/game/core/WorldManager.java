package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
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

    private Array<BoundingBox> collisionBoxes;
    private int collisionCount = 0;

    private ShapeRenderer debugRenderer;

    // ==========================================================
    // FIX LAG: Matikan saklar ini (false) agar garis merah tidak dirender
    public boolean isDebugMode = false;

    public WorldManager(GameContext context) {
        this.context = context;
        this.collisionBoxes = new Array<>();
    }

    public void initialize(SceneRenderer renderer) {
        try {
            debugRenderer = new ShapeRenderer();

            mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Maps3.glb"));
            mapScene = new Scene(mapAsset.scene);

            mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
            mapScene.modelInstance.calculateTransforms();

            collisionCount = 0;

            Gdx.app.log("WORLD", "Memulai Ekstraksi Collision...");
            extractAndHideCollisions(mapScene.modelInstance.nodes);

            renderer.addScene(mapScene);
            Gdx.app.log("WORLD", "Map berhasil diload! TOTAL TEMBOK GAIB TERDETEKSI: " + collisionCount);

        } catch (Exception e) {
            Gdx.app.log("WORLD_WARNING", "Map.glb tidak ditemukan, abaikan jika masih dummy.", e);
        }
    }

    private void extractAndHideCollisions(Iterable<Node> nodes) {
        for (Node node : nodes) {
            String nodeName = node.id != null ? node.id.toLowerCase() : "unnamed";

            if (nodeName.contains("pohon_col") || nodeName.contains("collider")) {

                BoundingBox box = new BoundingBox();

                // 1. CALCULATE BOUNDS: Ini menghitung kotak dalam skala lokal (1x)
                node.calculateBoundingBox(box, true);

                if (box.isValid()) {
                    // 2. KUNCI UTAMA:
                    // Kalikan Bounding Box dengan skala dunia (10x lipat) agar posisinya dan ukurannya pas di Map!
                    box.mul(mapScene.modelInstance.transform);

                    collisionBoxes.add(box);
                    collisionCount++;
                }

                // 3. Hancurkan wujud tabung putihnya
                hideNodeAndChildren(node);

            } else if (node.hasChildren()) {
                extractAndHideCollisions(node.getChildren());
            }
        }
    }

    private void hideNodeAndChildren(Node node) {
        node.parts.clear();

        // TRIK KASAR AAA: Karena GLTF kadang bandel, kita paksa hancurkan ukurannya jadi 0
        // dan kita lemparkan objeknya ke bawah tanah sejauh -9999 meter!
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

        // Pencarian tabrakan di RAM (CPU), ini SANGAT RINGAN dan tidak akan bikin lag!
        for (BoundingBox wallBox : collisionBoxes) {
            if (wallBox.intersects(entityBox)) {
                return true;
            }
        }
        return false;
    }

    public void update(float fixedDelta) {}

    public void renderDebug(PerspectiveCamera cam) {
        // Karena isDebugMode = false, kode di bawah ini langsung di-skip (FPS langsung naik drastis!)
        if (!isDebugMode || debugRenderer == null || collisionBoxes.size == 0) return;

        debugRenderer.setProjectionMatrix(cam.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.RED);

        for (BoundingBox box : collisionBoxes) {
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
        collisionBoxes.clear();
    }
}
