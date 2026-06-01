package com.bismillahjuara.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.core.RenderPipeline;
import com.bismillahjuara.game.core.SceneRenderer;
import com.bismillahjuara.game.core.UpdatePipeline;
import com.bismillahjuara.game.entity.EnvironmentProp;
import com.bismillahjuara.game.entity.KunangKunang;
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

    public Runnable onAct1Cinematic;
    public Runnable onAct2Cinematic;
    public Runnable onEnding1Cinematic;
    public Runnable onEnding2Cinematic;

    public GameplayManager() {
        context = new GameContext();
    }

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

        // 1. AMBIL ASET YANG SUDAH DI-LOAD (Aman anti-crash)
        SceneAsset playerAsset = GameAssets.getInstance().manager.get(GameAssets.PLAYER_GLB, SceneAsset.class);
        SceneAsset enemyAsset = GameAssets.getInstance().manager.get(GameAssets.ENEMY_GLB, SceneAsset.class);

        SceneAsset homeAsset = null;
        SceneAsset templeAsset = null;
        try {
            if (GameAssets.getInstance().manager.isLoaded(GameAssets.HOME_GLB))
                homeAsset = GameAssets.getInstance().manager.get(GameAssets.HOME_GLB, SceneAsset.class);
            if (GameAssets.getInstance().manager.isLoaded(GameAssets.TEMPLE_GLB))
                templeAsset = GameAssets.getInstance().manager.get(GameAssets.TEMPLE_GLB, SceneAsset.class);
        } catch (Exception e) {
            Gdx.app.log("SPAWN_ENV", "Home/Temple belum di-load di GameAssets, dilompati.");
        }

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
// SPAWN RUMAH UTAMA
// ====================================================================
        if (homeAsset != null) {

            // Ditarik lebih jauh karena ukurannya sekarang jauh lebih besar
            Vector3 homePos = new Vector3(centerSafePos).add(0f, 0f, -40f);

            // Sebelumnya 10x, sekarang 35x
            Vector3 homeScale = new Vector3(35f, 35f, 35f);

            EnvironmentProp home = new EnvironmentProp(
                homePos,
                new Vector3(0f, 0f, 0f),
                homeScale,
                context,
                homeAsset
            );

            context.entityManager.addEntity(home);

            Gdx.app.log(
                "DEBUG_HOME",
                "Rumah berhasil di-spawn! Pos: "
                    + homePos
                    + " | Scale: "
                    + homeScale
            );
        }


// ====================================================================
// SPAWN 10 CANDI MIRING
// ====================================================================
        if (templeAsset != null) {

            for (int i = 0; i < 10; i++) {

                Vector3 pos = new Vector3();
                context.worldManager.getRandomSafePosition(pos);

                Vector3 rot = new Vector3(
                    MathUtils.random(-15f, 15f),
                    MathUtils.random(0f, 360f),
                    MathUtils.random(-15f, 15f)
                );

                // Sebelumnya 5x, sekarang 20x
                Vector3 templeScale = new Vector3(
                    20f,
                    20f,
                    20f
                );

                EnvironmentProp temple = new EnvironmentProp(
                    pos,
                    rot,
                    templeScale,
                    context,
                    templeAsset
                );

                context.entityManager.addEntity(temple);

                Gdx.app.log(
                    "DEBUG_TEMPLE",
                    "Temple #" + i
                        + " spawned at "
                        + pos
                        + " | Scale: "
                        + templeScale
                );
            }
        }

        // --- FIX AAA: SPAWN 150 KUNANG-KUNANG ---
        for (int i = 0; i < 150; i++) {
            Vector3 bugPos = new Vector3();
            context.worldManager.getRandomSafePosition(bugPos);
            bugPos.y += MathUtils.random(0.5f, 4.0f);
            context.entityManager.addEntity(new KunangKunang(bugPos, context));
        }

        // ====================================================================
        // 3. SPAWN 3 RELIK PUSAKA (Keris, Kujang, Mandau)
        // ====================================================================
        RelicPusaka.RelicType[] relicTypes = {RelicPusaka.RelicType.KERIS, RelicPusaka.RelicType.KUJANG, RelicPusaka.RelicType.MANDAU};
        String[] relicPaths = {GameAssets.KERIS_GLB, GameAssets.KUJANG_GLB, GameAssets.MANDAU_GLB};

        for (int i = 0; i < 3; i++) {
            Vector3 relicPos = new Vector3();
            boolean rValid = false;
            int rRetries = 50;

            while (!rValid && rRetries > 0) {
                context.worldManager.getRandomSafePosition(relicPos);
                rValid = true;

                // Relic harus agak jauh dari player biar dicari
                if (relicPos.dst(centerSafePos) < 30f) rValid = false;
                if (rValid && context.worldManager.isColliding(relicPos, 1.0f, 1.0f)) rValid = false;

                rRetries--;
            }

            if (rValid) {
                // Ambil aset 3D yang sesuai
                SceneAsset relicAsset = null;
                try {
                    if (GameAssets.getInstance().manager.isLoaded(relicPaths[i])) {
                        relicAsset = GameAssets.getInstance().manager.get(relicPaths[i], SceneAsset.class);
                    }
                } catch (Exception e) {}

                // Spawn Entitas
                RelicPusaka relic = new RelicPusaka(relicPos, context, relicAsset, relicTypes[i]);
                context.entityManager.addEntity(relic);

                Gdx.app.log("SPAWN_RELIC", "RELIC SPAWNED | Type: " + relicTypes[i].name() + " | Position: " + relicPos);
            }
        }

        // 4. SPAWN 10 SUKMA GOWONG
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
        Gdx.app.log("PROFILE_GAMESCREEN", "Entitas Lingkungan & Karakter selesai dalam: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void spawnButoIjo() {
        Vector3 spawnPos = new Vector3();
        context.worldManager.getRandomSafePosition(spawnPos);

        SceneAsset bossAsset = GameAssets.getInstance().manager.get(GameAssets.BUTO_GLB, SceneAsset.class);

        context.boss = new com.bismillahjuara.game.entity.ButoIjo(spawnPos, context, bossAsset);
        context.entityManager.addEntity(context.boss);
    }

    public void bindInput(GameInputHandler inputHandler) {
        context.inputHandler = inputHandler;
    }

    public void startGameplay() {
//        context.state = GameplayState.CUTSCENE;
//        if (onAct1Cinematic != null) onAct1Cinematic.run();

        context.currentAct = com.bismillahjuara.game.screens.StoryMenuScreen.DEBUG_START_ACT;

        if (context.currentAct == 2) {
            // JIKA MULAI LANGSUNG DARI ACT 2:
            context.relicsCollected = 3;
            context.state = GameplayState.CUTSCENE;
            if (onAct2Cinematic != null) onAct2Cinematic.run();
        } else {
            // JIKA MULAI NORMAL DARI ACT 1:
            context.state = GameplayState.CUTSCENE;
            if (onAct1Cinematic != null) onAct1Cinematic.run();
        }
    }

    public void pauseGame() {
        if (context.state == GameplayState.PLAYING) {
            context.state = GameplayState.PAUSED;
        }
    }

    public void resumeGame() {
        if (context.state == GameplayState.PAUSED || context.state == GameplayState.CUTSCENE) {
            context.state = GameplayState.PLAYING;
        }
    }

    public GameContext getContext() {
        return context;
    }

    public void update(float delta) {
        if (updatePipeline != null) updatePipeline.update(delta);

        if (context.state == GameplayState.PLAYING && !context.isEndingTriggered) {

            // CEK TRANSISI ACT 1 -> ACT 2
            if (context.currentAct == 1 && context.relicsCollected >= 3) {
                context.currentAct = 2;
                context.state = GameplayState.CUTSCENE;
                if (onAct2Cinematic != null) onAct2Cinematic.run();
            }

            // CEK ENDING ACT 2
            if (context.currentAct == 2 && context.boss != null) {

                if (!context.boss.isDead() && context.player.getPosition().dst(context.boss.getPosition()) < 2.0f) {
                    context.isEndingTriggered = true;
                    context.state = GameplayState.CUTSCENE;
                    if (onEnding1Cinematic != null) onEnding1Cinematic.run();
                }

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
