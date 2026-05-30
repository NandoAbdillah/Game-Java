package com.bismillahjuara.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.entity.Player;
import com.bismillahjuara.game.entity.SukmaGowong;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.managers.EntityManager;
import com.bismillahjuara.game.managers.WorldManager;

public class GameplayManager {

    private GameContext context;

    private UpdatePipeline updatePipeline;
    private RenderPipeline renderPipeline;

    public GameplayManager() {
        context = new GameContext();
        context.sceneRenderer = new SceneRenderer(); // Instansiasi lebih dulu

        context.worldManager = new WorldManager(context);
        context.entityManager = new EntityManager(context);

        updatePipeline = new UpdatePipeline(context);
        renderPipeline = new RenderPipeline(context, context.sceneRenderer);
    }

    public void buildWorld() {
        context.worldManager.initialize(context.sceneRenderer);
    }

    public void buildEntities(AdvancedCameraSystem camera) {
        context.camera = camera;

        // ====================================================================
        // 1. SPAWN PLAYER DI PUSAT SAFE AREA (ANTI-NYANGKUT)
        // ====================================================================
        context.player = new Player(context);
        Vector3 centerSafePos = new Vector3();

        // Minta titik tengah yang benar-benar aman dari WorldManager!
        context.worldManager.getSafeCenterPosition(centerSafePos);

        // Failsafe: Pastikan titik tengah absolut tidak bertabrakan dengan batu/pohon
        int playerRetries = 10;
        while (context.worldManager.isColliding(centerSafePos, 0.5f, 2.0f) && playerRetries > 0) {
            centerSafePos.add(1.5f, 0, 1.5f); // Geser perlahan jika nyangkut
            playerRetries--;
        }

        context.player.getPosition().set(centerSafePos);
        context.worldManager.playerSpawnPos.set(centerSafePos);

        boolean isPlayerValid = !context.worldManager.isColliding(centerSafePos, 0.5f, 2.0f);
        Gdx.app.log("SPAWN_VALIDATION", "Player Spawn Valid = " + isPlayerValid + " | Pos: " + centerSafePos);


        // ====================================================================
        // 2. SPAWN 10 SUKMA GOWONG DENGAN VALIDASI KETAT
        // ====================================================================
        context.worldManager.enemySpawnPositions.clear();
        Array<Vector3> spawnedPositions = new Array<>();

        for (int i = 0; i < 10; i++) {
            Vector3 spawnPos = new Vector3();
            boolean valid = false;
            int retries = 50; // Maksimal cari 50 titik untuk 1 musuh agar tidak Infinite Loop

            while (!valid && retries > 0) {
                // Minta titik acak yang DI DALAM batas aman map!
                context.worldManager.getRandomSafePosition(spawnPos);
                valid = true;

                // Syarat 1: Minimal 20 meter dari Player
                if (spawnPos.dst(centerSafePos) < 20f) {
                    valid = false;
                }

                // Syarat 2: Minimal 5 meter dari musuh lain
                if (valid) {
                    for (Vector3 otherPos : spawnedPositions) {
                        if (spawnPos.dst(otherPos) < 5f) {
                            valid = false;
                            break;
                        }
                    }
                }

                // Syarat 3: Tidak boleh menabrak pohon/obstacle (IsColliding)
                if (valid) {
                    if (context.worldManager.isColliding(spawnPos, 0.5f, 2.0f)) {
                        valid = false;
                    }
                }

                retries--;
            }

            if (valid) {
                float distToBorder = Math.min(
                    Math.abs(spawnPos.x - (context.worldManager.mapBounds.max.x - context.worldManager.safePlayMargin)),
                    Math.abs(spawnPos.z - (context.worldManager.mapBounds.max.z - context.worldManager.safePlayMargin))
                );

                Gdx.app.log("SPAWN_VALIDATION", "SukmaGowong [" + i + "] Spawn Valid = TRUE | DistToPlayer: "
                    + String.format("%.1f", spawnPos.dst(centerSafePos)) + "m | DistToBorder: ~"
                    + String.format("%.1f", distToBorder) + "m");

                spawnedPositions.add(new Vector3(spawnPos));
                context.worldManager.enemySpawnPositions.add(new Vector3(spawnPos));

                SukmaGowong enemy = new SukmaGowong(spawnPos, context);
                context.entityManager.addEntity(enemy);
            } else {
                Gdx.app.log("SPAWN_VALIDATION", "SukmaGowong [" + i + "] Spawn Valid = FALSE (Gagal cari ruang setelah 50x coba)");
            }
        }
    }

    public void bindInput(GameInputHandler inputHandler) {
        context.inputHandler = inputHandler;
    }

    public void startGameplay() {
        context.state = GameplayState.PLAYING;
    }

    // ==========================================================
    // --- PAUSE API (JEMBATAN UNTUK GAME SCREEN) ---
    // ==========================================================
    public void pauseGame() {
        if (context.state == GameplayState.PLAYING) {
            context.state = GameplayState.PAUSED;
        }
    }

    public void resumeGame() {
        if (context.state == GameplayState.PAUSED) {
            context.state = GameplayState.PLAYING;
        }
    }

    public GameContext getContext() {
        return context;
    }
    // ==========================================================

    public void update(float delta) {
        updatePipeline.update(delta);
    }

    public void render(float delta) {
        renderPipeline.render(delta);
    }

    public void dispose() {
        context.sceneRenderer.dispose();
        context.worldManager.dispose();
        context.entityManager.dispose();
        if (context.player != null) context.player.dispose();
    }
}
