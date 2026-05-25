package com.bismillahjuara.game.assets;

import com.badlogic.gdx.assets.AssetManager;


public class GameAssets {

    private static GameAssets instance;
    public final AssetManager manager;

    public static final String MODEL_PLAYER = "models/chars/TimunAnim.glb";
    public static final String MODEL_ENEMY = "models/enemies/SukmaGowong.glb";
    public static final String MODEL_MAP = "models/maps/Maps.glb";

    private GameAssets() {
        manager = new AssetManager();
        // TODO: Di fase "Asset Streaming" sesungguhnya, kita harus mendaftarkan
        // SceneAssetLoader dari MGSX ke manager ini agar GLB diload di background thread.
    }

    public static GameAssets getInstance() {
        if (instance == null) {
            instance = new GameAssets();
        }
        return instance;
    }

    public void queueBootAssets() {
        // Aset untuk Splash dan Loading
    }

    public void queueGameplayAssets() {
        // saat ini, model 3D (Player, Map, Musuh) masih di-load secara hardcode di constructor masing" kelas.
        // Metode ini disiapkan agar AsyncGameplayLoader tidak error (Method Not Found)

        // Nanto kita mendaftarkan sesuatu agar AssetManager berjalan.
        // Nanti setelah refactor "True Async 3D", kita masukkan antreannya di sini
    }

    public void dispose() {
        manager.dispose();
    }
}
