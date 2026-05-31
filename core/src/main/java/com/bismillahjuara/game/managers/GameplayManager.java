package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.core.RenderPipeline;
import com.bismillahjuara.game.core.SceneRenderer;
import com.bismillahjuara.game.core.UpdatePipeline;
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
    }

    // =========================================================================
    // BARU: FUNGSI KHUSUS UNTUK ASYNC LOADER
    // =========================================================================
    public void buildWorldCore() {
        // FIX AAA: Semua inisialisasi krusial ditaruh di sini agar dijamin 1000% TIDAK NULL
        // saat AsyncLoader memanggil tahap INIT_ENTITIES.
        context.sceneRenderer = new SceneRenderer();
        context.worldManager = new WorldManager(context);
        context.entityManager = new EntityManager(context); // <--- KUNCI FIX ANTI CRASH!

        renderPipeline = new RenderPipeline(context, context.sceneRenderer);
        updatePipeline = new UpdatePipeline(context);
    }

    public void buildEntities(AdvancedCameraSystem camera) {
        // FAILSAFE SUPER AMAN: Jika masih null (karena alasan apapun), paksa buat baru!
        if (context.entityManager == null) {
            context.entityManager = new EntityManager(context);
        }

        context.camera = camera;

        // ====================================================================
        // 1. SPAWN PLAYER DI PUSAT SAFE AREA (ANTI-NYANGKUT)
        // ====================================================================
        context.player = new Player(context);
        Vector3 centerSafePos = new Vector3();

        context.worldManager.getSafeCenterPosition(centerSafePos);

        int playerRetries = 10;
        while (context.worldManager.isColliding(centerSafePos, 0.5f, 2.0f) && playerRetries > 0) {
            centerSafePos.add(1.5f, 0, 1.5f);
            playerRetries--;
        }

        context.player.getPosition().set(centerSafePos);
        context.worldManager.playerSpawnPos.set(centerSafePos);

        // ====================================================================
        // 2. SPAWN 10 SUKMA GOWONG DENGAN VALIDASI KETAT
        // ====================================================================
        context.worldManager.enemySpawnPositions.clear();
        Array<Vector3> spawnedPositions = new Array<>();

        for (int i = 0; i < 10; i++) {
            Vector3 spawnPos = new Vector3();
            boolean valid = false;
            int retries = 50;

            while (!valid && retries > 0) {
                context.worldManager.getRandomSafePosition(spawnPos);
                valid = true;

                if (spawnPos.dst(centerSafePos) < 20f) valid = false;

                if (valid) {
                    for (Vector3 otherPos : spawnedPositions) {
                        if (spawnPos.dst(otherPos) < 5f) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid && context.worldManager.isColliding(spawnPos, 0.5f, 2.0f)) {
                    valid = false;
                }

                retries--;
            }

            if (valid) {
                spawnedPositions.add(new Vector3(spawnPos));
                context.worldManager.enemySpawnPositions.add(new Vector3(spawnPos));

                SukmaGowong enemy = new SukmaGowong(spawnPos, context);
                // INI YANG TADI BIKIN CRASH! Sekarang dijamin sukses 100%
                context.entityManager.addEntity(enemy);
            }
        }
    }

    public void bindInput(GameInputHandler inputHandler) {
        context.inputHandler = inputHandler;
    }

    public void startGameplay() {
        context.state = GameplayState.PLAYING;
    }

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

    public void update(float delta) {
        if (updatePipeline != null) updatePipeline.update(delta);
    }

    public void render(float delta) {
        if (renderPipeline != null) renderPipeline.render(delta);
    }

    public void dispose() {
        if (context.sceneRenderer != null) context.sceneRenderer.dispose();
        if (context.worldManager != null) context.worldManager.dispose();
        if (context.entityManager != null) context.entityManager.dispose();
        if (context.player != null) context.player.dispose();
    }
}
