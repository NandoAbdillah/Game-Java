package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.core.SceneRenderer;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Mengelola Environment, Terrain, dan Cuaca.
 * Disiapkan untuk World Streaming (membaca chunk peta secara dinamis).
 */
public class WorldManager {

    private GameContext context;
    private SceneAsset mapAsset;
    private Scene mapScene;

    public WorldManager(GameContext context) {
        this.context = context;
    }

    public void initialize(SceneRenderer renderer) {
        // TODO: Di masa depan gunakan GameAssets AssetManager agar Async!
        try {
            mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Map.glb"));
            mapScene = new Scene(mapAsset.scene);
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
