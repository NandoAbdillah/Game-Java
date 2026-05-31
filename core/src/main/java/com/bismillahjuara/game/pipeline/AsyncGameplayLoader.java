package com.bismillahjuara.game.pipeline;

import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.screens.GameScreen;

/**
 * Time-Sliced Gameplay Initializer dengan Profiler Bawaan.
 * Memecah pembuatan World menjadi beberapa frame agar Render Thread tidak freeze.
 */
public class AsyncGameplayLoader {

    public enum LoadState {
        QUEUE_ASSETS,           // 0.0 - 0.1
        WAIT_ASSETS,            // 0.1 - 0.3
        INIT_SHADERS,           // 0.3 - 0.4
        INIT_WORLD_READ_MAP,    // 0.4 - 0.5 (I/O)
        INIT_WORLD_BUILD_SCENE, // 0.5 - 0.6 (GLTF Scene)
        INIT_WORLD_SCAN_NODES,  // 0.6 - 0.8 (Async Node Scanner)
        INIT_ENTITIES,          // 0.8 - 0.9
        INIT_UI,                // 0.9 - 1.0
        DONE
    }

    private LoadState currentState = LoadState.QUEUE_ASSETS;
    private GameScreen targetScreen;
    private float progress = 0f;

    // Untuk Profiling
    private long stepStartTime;
    private long globalStartTime;

    public AsyncGameplayLoader() {
        targetScreen = new GameScreen();
        globalStartTime = System.currentTimeMillis();
    }

    private void beginStep(String msg) {
        Gdx.app.log("PROFILE_LOADER", ">>> Memulai: " + msg);
        stepStartTime = System.currentTimeMillis();
    }

    private void endStep(String msg) {
        long duration = System.currentTimeMillis() - stepStartTime;
        Gdx.app.log("PROFILE_LOADER", "<<< Selesai: " + msg + " (" + duration + " ms)");
    }

    public void update() {
        switch (currentState) {
            case QUEUE_ASSETS:
                beginStep("Queue Assets");
                GameAssets.getInstance().queueGameplayAssets();
                endStep("Queue Assets");

                progress = 0.1f;
                currentState = LoadState.WAIT_ASSETS;
                break;

            case WAIT_ASSETS:
                if (GameAssets.getInstance().manager.update()) {
                    progress = 0.3f;
                    currentState = LoadState.INIT_SHADERS;
                } else {
                    progress = 0.1f + (GameAssets.getInstance().manager.getProgress() * 0.2f);
                }
                break;

            case INIT_SHADERS:
                beginStep("Shader Warmup");
                com.bismillahjuara.game.assets.ShaderWarmup.executeWarmup();
                endStep("Shader Warmup");

                progress = 0.4f;
                currentState = LoadState.INIT_WORLD_READ_MAP;
                break;

            case INIT_WORLD_READ_MAP:
                beginStep("Baca GLB Map Disk");
                // Membaca file 3D ke memory (Bisa 100-300ms, tapi tidak membuat Android ANR karena hanya 1 frame)
                targetScreen.getGameplayManager().buildWorldCore(); // Menyiapkan WorldManager murni
                targetScreen.getGameplayManager().getContext().worldManager.step1_LoadMapDisk();
                endStep("Baca GLB Map Disk");

                progress = 0.5f;
                currentState = LoadState.INIT_WORLD_BUILD_SCENE;
                break;

            case INIT_WORLD_BUILD_SCENE:
                beginStep("Build Scene GLTF");
                // Ekstraksi 3D Node
                targetScreen.getGameplayManager().getContext().worldManager.step2_BuildSceneAndQueue(
                    targetScreen.getGameplayManager().getContext().sceneRenderer
                );
                endStep("Build Scene GLTF");

                progress = 0.6f;
                currentState = LoadState.INIT_WORLD_SCAN_NODES;
                break;

            case INIT_WORLD_SCAN_NODES:
                // TAHAP INI DIEKSEKUSI BERKALI-KALI (TIME-SLICED).
                // Sangat ringan (Maksimal 15ms per frame)
                boolean isScanDone = targetScreen.getGameplayManager().getContext().worldManager.step3_ProcessNodesAsync();

                // Animasi bar 0.6 hingga 0.8
                float scanProgress = targetScreen.getGameplayManager().getContext().worldManager.getAsyncProgress();
                progress = 0.6f + (scanProgress * 0.2f);

                if (isScanDone) {
                    currentState = LoadState.INIT_ENTITIES;
                }
                break;

            case INIT_ENTITIES:
                beginStep("Init Entities (Musuh & Player)");
                targetScreen.initCamera();
                targetScreen.initEntities();
                endStep("Init Entities (Musuh & Player)");

                progress = 0.9f;
                currentState = LoadState.INIT_UI;
                break;

            case INIT_UI:
                beginStep("Init UI & HUD");
                targetScreen.initUI();
                endStep("Init UI & HUD");

                progress = 1.0f;
                currentState = LoadState.DONE;
                long totalTime = System.currentTimeMillis() - globalStartTime;
                Gdx.app.log("PROFILE_LOADER", "TOTAL WAKTU LOADING SELESAI: " + totalTime + " ms!");
                break;

            case DONE:
                break;
        }
    }

    public float getProgress() { return progress; }
    public boolean isDone() { return currentState == LoadState.DONE; }
    public GameScreen getReadyGameScreen() { return targetScreen; }
}
