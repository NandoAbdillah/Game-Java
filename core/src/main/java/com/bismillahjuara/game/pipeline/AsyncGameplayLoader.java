package com.bismillahjuara.game.pipeline;

import com.badlogic.gdx.Gdx;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.screens.GameScreen;

public class AsyncGameplayLoader {

    public enum LoadState {
        QUEUE_ASSETS,
        WAIT_ASSETS,
        INIT_SHADERS,
        INIT_WORLD_BUILD_SCENE, // Load Disk dibuang, langsung Build Scene
        INIT_WORLD_SCAN_NODES,
        INIT_ENTITIES,
        INIT_UI,
        DONE
    }

    private LoadState currentState = LoadState.QUEUE_ASSETS;
    private GameScreen targetScreen;
    private float progress = 0f;
    private long globalStartTime;

    public AsyncGameplayLoader() {
        targetScreen = new GameScreen();
        globalStartTime = System.currentTimeMillis();
    }

    public void update() {
        switch (currentState) {
            case QUEUE_ASSETS:
                GameAssets.getInstance().queueGameplayAssets();
                progress = 0.1f;
                currentState = LoadState.WAIT_ASSETS;
                break;

            case WAIT_ASSETS:
                // Ini akan membaca Map, Player, Enemy, dan Audio di latar belakang tanpa Freeze!
                if (GameAssets.getInstance().manager.update()) {
                    progress = 0.4f;
                    currentState = LoadState.INIT_SHADERS;
                } else {
                    progress = 0.1f + (GameAssets.getInstance().manager.getProgress() * 0.3f);
                }
                break;

            case INIT_SHADERS:
                com.bismillahjuara.game.assets.ShaderWarmup.executeWarmup();
                progress = 0.5f;
                // Siapkan kerangka manager utama
                targetScreen.getGameplayManager().buildWorldCore();
                currentState = LoadState.INIT_WORLD_BUILD_SCENE;
                break;

            case INIT_WORLD_BUILD_SCENE:
                // Mengambil Map dari cache, membangun scene 22k nodes (Masih butuh beberapa detik, tapi tanpa I/O)
                targetScreen.getGameplayManager().getContext().worldManager.step2_BuildSceneAndQueue(
                    targetScreen.getGameplayManager().getContext().sceneRenderer
                );
                progress = 0.6f;
                currentState = LoadState.INIT_WORLD_SCAN_NODES;
                break;

            case INIT_WORLD_SCAN_NODES:
                boolean isScanDone = targetScreen.getGameplayManager().getContext().worldManager.step3_ProcessNodesAsync();
                progress = 0.6f + (targetScreen.getGameplayManager().getContext().worldManager.getAsyncProgress() * 0.2f);

                if (isScanDone) {
                    currentState = LoadState.INIT_ENTITIES;
                }
                break;

            case INIT_ENTITIES:
                targetScreen.initCamera();
                targetScreen.initEntities(); // SEKARANG CUMA BUTUH < 50ms !!!
                progress = 0.9f;
                currentState = LoadState.INIT_UI;
                break;

            case INIT_UI:
                targetScreen.initUI();
                progress = 1.0f;
                currentState = LoadState.DONE;
                Gdx.app.log("PROFILE_LOADER", "TOTAL LOADING SELESAI: " + (System.currentTimeMillis() - globalStartTime) + " ms!");
                break;

            case DONE:
                break;
        }
    }

    public float getProgress() { return progress; }
    public boolean isDone() { return currentState == LoadState.DONE; }
    public GameScreen getReadyGameScreen() { return targetScreen; }
}
