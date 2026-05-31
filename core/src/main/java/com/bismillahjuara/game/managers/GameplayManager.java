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
import com.bismillahjuara.game.entity.RelicPusaka;
import com.bismillahjuara.game.entity.SukmaGowong;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.assets.GameAssets;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameplayManager {

    private GameContext context;

    private UpdatePipeline updatePipeline;
    private RenderPipeline renderPipeline;

    // --- TAMBAHAN STORY CALLBACKS ---
    public Runnable onAct1Cinematic;
    public Runnable onAct2Cinematic;
    public Runnable onEnding1Cinematic;
    public Runnable onEnding2Cinematic;

    public GameplayManager() {
        context = new GameContext();
    }

    // =========================================================================
    // BARU: FUNGSI KHUSUS UNTUK ASYNC LOADER
    // =========================================================================
    public void buildWorldCore() {
        context.sceneRenderer = new SceneRenderer();
        context.worldManager = new WorldManager(context);
        context.entityManager = new EntityManager(context);

        renderPipeline = new RenderPipeline(context, context.sceneRenderer);
        updatePipeline = new UpdatePipeline(context);
    }

    public void buildEntities(AdvancedCameraSystem camera) {
        if (context.entityManager == null) {
            context.entityManager = new EntityManager(context);
        }
        context.camera = camera;

        long startTime = System.currentTimeMillis();

        // 1. AMBIL ASET YANG SUDAH DI-LOAD KE RAM
        SceneAsset playerAsset = GameAssets.getInstance().manager.get(GameAssets.PLAYER_GLB, SceneAsset.class);
        SceneAsset enemyAsset = GameAssets.getInstance().manager.get(GameAssets.ENEMY_GLB, SceneAsset.class);

        // 2. SPAWN PLAYER
        context.player = new Player(context, playerAsset);

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
        // SPAWN 3 RELIK PUSAKA (ACT 1)
        // ====================================================================
        // FIX: Hardcode 3 agar tidak error karena RELICS_NEEDED tidak ada di GameContext
        for (int i = 0; i < 3; i++) {
            Vector3 relicPos = new Vector3();
            boolean rValid = false;
            int rRetries = 50;

            while (!rValid && rRetries > 0) {
                context.worldManager.getRandomSafePosition(relicPos);
                rValid = true;

                // Jangan terlalu dekat dengan player (biar nyari)
                if (relicPos.dst(centerSafePos) < 30f) rValid = false;
                if (rValid && context.worldManager.isColliding(relicPos, 1.0f, 1.0f)) rValid = false;

                rRetries--;
            }
            if (rValid) {
                context.entityManager.addEntity(new RelicPusaka(relicPos, context));
            }
        }

        // 3. SPAWN 10 SUKMA GOWONG
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
                        if (spawnPos.dst(otherPos) < 5f) { valid = false; break; }
                    }
                }
                if (valid && context.worldManager.isColliding(spawnPos, 0.5f, 2.0f)) valid = false;
                retries--;
            }

            if (valid) {
                spawnedPositions.add(new Vector3(spawnPos));
                context.worldManager.enemySpawnPositions.add(new Vector3(spawnPos));

                SukmaGowong enemy = new SukmaGowong(spawnPos, context, enemyAsset);
                context.entityManager.addEntity(enemy);
            }
        }
        Gdx.app.log("PROFILE_GAMESCREEN", "11 Entitas di-spawn dalam: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void spawnButoIjo() {
        Vector3 spawnPos = new Vector3();
        context.worldManager.getRandomSafePosition(spawnPos);

        SceneAsset bossAsset = GameAssets.getInstance().manager.get(GameAssets.ENEMY_GLB, SceneAsset.class);

        context.boss = new com.bismillahjuara.game.entity.ButoIjo(spawnPos, context, bossAsset);
        context.entityManager.addEntity(context.boss);
    }

    public void bindInput(GameInputHandler inputHandler) {
        context.inputHandler = inputHandler;
    }

    public void startGameplay() {
        context.state = GameplayState.CUTSCENE;
        if (onAct1Cinematic != null) onAct1Cinematic.run();
    }

    public void pauseGame() {
        if (context.state == GameplayState.PLAYING) {
            context.state = GameplayState.PAUSED;
        }
    }

    public void resumeGame() {
        // FIX AAA: Izinkan transisi dari CUTSCENE kembali ke PLAYING juga!
        if (context.state == GameplayState.PAUSED || context.state == GameplayState.CUTSCENE) {
            context.state = GameplayState.PLAYING;
        }
    }

    public GameContext getContext() {
        return context;
    }

    public void update(float delta) {
        if (updatePipeline != null) updatePipeline.update(delta);

        // --- STORY STATE CHECKER ---
        if (context.state == GameplayState.PLAYING && !context.isEndingTriggered) {

            // CEK TRANSISI ACT 1 -> ACT 2
            if (context.currentAct == 1 && context.relicsCollected >= 3) {
                context.currentAct = 2;
                context.state = GameplayState.CUTSCENE;
                if (onAct2Cinematic != null) onAct2Cinematic.run();
            }

            // CEK ENDING ACT 2
            if (context.currentAct == 2 && context.boss != null) {

                // ENDING 1: Ketangkep! (Jarak < 2 meter)
                if (!context.boss.isDead() && context.player.getPosition().dst(context.boss.getPosition()) < 2.0f) {
                    context.isEndingTriggered = true;
                    context.state = GameplayState.CUTSCENE;
                    if (onEnding1Cinematic != null) onEnding1Cinematic.run();
                }

                // ENDING 2: Buto Ijo Mati (10 Hits)
                if (context.boss.isDead()) {
                    context.isEndingTriggered = true;
                    context.state = GameplayState.CUTSCENE;
                    if (onEnding2Cinematic != null) onEnding2Cinematic.run();
                }
            }
        }
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
