package com.bismillahjuara.game.pipeline;

import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.screens.GameScreen;

/**
 * Time-Sliced Gameplay Initializer.
 * Memecah pembuatan World, Player, dan Shader menjadi beberapa frame
 * agar Render Thread tidak pernah freeze (Anti-ANR Android).
 */
public class AsyncGameplayLoader {

    public enum LoadState {
        QUEUE_ASSETS,   // Mendaftarkan file fisik ke AssetManager
        WAIT_ASSETS,    // Menunggu AssetManager load file di background thread
        INIT_SHADERS,   // Kompilasi shader PBR (Berat, dikerjakan 1 frame sendiri)
        INIT_WORLD,     // Generate terrain & pohon (Berat, dikerjakan 1 frame sendiri)
        INIT_ENTITIES,  // Spawn Player & Musuh
        INIT_UI,        // Setup HUD & Controller
        DONE
    }

    private LoadState currentState = LoadState.QUEUE_ASSETS;
    private GameScreen targetScreen;
    private float progress = 0f;

    public AsyncGameplayLoader() {
        // Kita HANYA membuat wadahnya, isinya kosong. Tidak ada beban memori di sini.
        targetScreen = new GameScreen();
    }

    /**
     * Dipanggil setiap frame oleh Layar Loading.
     * Menggunakan logika Switch-Case agar hanya 1 pekerjaan berat yang dieksekusi per frame.
     */
    public void update() {
        switch (currentState) {
            case QUEUE_ASSETS:
                Gdx.app.log("LOADER", "[1/6] Mengantre aset GLTF & Texture...");
                GameAssets.getInstance().queueGameplayAssets(); // TODO: Buat method ini di GameAssets nanti
                progress = 0.1f;
                currentState = LoadState.WAIT_ASSETS;
                break;

            case WAIT_ASSETS:
                // AssetManager bekerja di background thread (aman dari freeze)
                if (GameAssets.getInstance().manager.update()) {
                    progress = 0.5f; // File selesai di-load ke RAM
                    currentState = LoadState.INIT_SHADERS;
                } else {
                    // Kalkulasi progress: 0.1 sampai 0.5 berdasarkan AssetManager
                    progress = 0.1f + (GameAssets.getInstance().manager.getProgress() * 0.4f);
                }
                break;

            case INIT_SHADERS:
                Gdx.app.log("LOADER", "[2/6] Memanaskan Shader PBR...");
                // Shader warmup yang tadinya di Boot, kita pindah ke sini secara modular
                com.bismillahjuara.game.assets.ShaderWarmup.executeWarmup();
                progress = 0.6f;
                currentState = LoadState.INIT_WORLD;
                break;

            case INIT_WORLD:
                Gdx.app.log("LOADER", "[3/6] Membangun World Map & Obstacle...");
                // TODO: Nanti di dalam GameScreen, buat method initWorld() yang dipanggil di sini
                targetScreen.initWorld();
                progress = 0.8f;
                currentState = LoadState.INIT_ENTITIES;
                break;

            case INIT_ENTITIES:
                Gdx.app.log("LOADER", "[4/6] Menyebarkan Musuh & Karakter...");
                targetScreen.initEntities();
                progress = 0.9f;
                currentState = LoadState.INIT_UI;
                break;

            case INIT_UI:
                Gdx.app.log("LOADER", "[5/6] Membangun HUD & Virtual Joystick...");
                targetScreen.initUI();
                progress = 1.0f;
                currentState = LoadState.DONE;
                break;

            case DONE:
                Gdx.app.log("LOADER", "[6/6] Gameplay Siap!");
                break;
        }
    }

    public float getProgress() { return progress; }
    public boolean isDone() { return currentState == LoadState.DONE; }
    public GameScreen getReadyGameScreen() { return targetScreen; }
}
