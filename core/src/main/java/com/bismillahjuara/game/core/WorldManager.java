package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.core.SceneRenderer;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/** Mengelola Environment, Terrain, dan Cuaca, Disiapkan untuk World Streaming (membaca chunk peta secara dinamis).*/
public class WorldManager {

    private GameContext context;
    private SceneAsset mapAsset;
    private Scene mapScene;

    private float mapScale = 10f;

    public WorldManager(GameContext context) {
        this.context = context;
    }

    public void initialize(SceneRenderer renderer) {

        try {
            mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Maps3.glb"));
            mapScene = new Scene(mapAsset.scene);
            mapScene.modelInstance.transform.setToScaling(mapScale, mapScale, mapScale);
            renderer.addScene(mapScene);
            Gdx.app.log("WORLD", "Map berhasil diload.");
        } catch (Exception e) {
            Gdx.app.log("WORLD_WARNING", "Map.glb tidak ditemukan, abaikan jika masih dummy.");
        }
    }

    public void update(float fixedDelta) {
        // TODO: Cek chunk yang perlu diload/unload berdasarkan jarak (Distance Culling)
        // TODO: Update cycle siang-malam / cuaca di sini
    }

    public void dispose() {
        if (mapAsset != null) mapAsset.dispose();
    }
}
