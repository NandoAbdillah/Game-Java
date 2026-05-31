package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.managers.GameplayManager;
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.transitions.FadeTransition;

public class GameScreen implements Screen {

    private GameplayManager gameplayManager;
    private HudManager hudManager;
    private GameInputHandler inputHandler;
    private AdvancedCameraSystem camera;

    // Jumpscare variables
    private boolean isJumpscareTriggered = false;
    private float jumpscareTimer = 0f;
    private Image jumpscareOverlay;
    private Texture bloodyTexture;

    public GameScreen() {
        gameplayManager = new GameplayManager();
    }

    public GameplayManager getGameplayManager() {
        return gameplayManager;
    }

    public void initCamera() {
        camera = new AdvancedCameraSystem();
    }

    public void initEntities() {
        long startTime = System.currentTimeMillis();
        gameplayManager.buildEntities(camera);
        Gdx.app.log("PROFILE_GAMESCREEN", "Init Entities selesai: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void initUI() {
        long startTime = System.currentTimeMillis();
        hudManager = new HudManager();
        inputHandler = new GameInputHandler(camera, hudManager);
        gameplayManager.bindInput(inputHandler);
        setupJumpscareUI();

        // ==========================================================
        // FIX TAHAP 1: WIRING PAUSE MENU CALLBACKS
        // Menghubungkan tombol Pause UI dengan fungsi logika di GameScreen
        // ==========================================================
        hudManager.getPauseMenuUI().onResumeCallback = new Runnable() {
            @Override public void run() { resumeGame(); }
        };
        hudManager.getPauseMenuUI().onMainMenuCallback = new Runnable() {
            @Override public void run() {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        };
        hudManager.getPauseMenuUI().onExitGameCallback = new Runnable() {
            @Override public void run() { Gdx.app.exit(); }
        };

        // --- TAMBAHAN PHASE 3: WIRING STORY CINEMATICS ---
        gameplayManager.onAct1Cinematic = new Runnable() {
            @Override public void run() {
                hudManager.getDebugUI().playCinematic(
                    "ACT I : PUSAKA YANG HILANG",
                    "Bukan sihir cahaya, melainkan benda-benda ritual peninggalan ibunya yang terasa berat dan mistis tersebar di dalam hutan.\n\nTimun harus menemukan seluruh pusaka tersebut jika ingin memiliki kekuatan untuk menghadapi Buto Ijo.",
                    12f, // FIX 1: Durasi DIPANJANGKAN jadi 12 detik agar teks selesai diketik!
                    new Runnable() { @Override public void run() { gameplayManager.resumeGame(); } }
                );
                // Matikan semua lagu lama, ganti ke lagu Theme untuk hutan
                AudioManager.getInstance().playMusic(com.bismillahjuara.game.audio.AudioTrack.THEME, 2f);
            }
        };

        gameplayManager.onAct2Cinematic = new Runnable() {
            @Override public void run() {
                hudManager.getDebugUI().playCinematic(
                    "ACT II : TIMUN REVENGE'S",
                    "Ibunya berpesan bahwa pusaka-pusaka tersebut menyimpan kekuatan yang cukup untuk membalas dendam.\n\nButo Ijo harus dihentikan sebelum semuanya terlambat. Lemparkan pusaka timun sebanyak 10 kali untuk mengalahkannya.",
                    10f, // FIX 1: Durasi DIPANJANGKAN
                    new Runnable() {
                        @Override public void run() {
                            gameplayManager.spawnButoIjo();
                            gameplayManager.resumeGame();
                        }
                    }
                );
                // Putar lagu Boss Fight (Asumsi kamu punya BATTLE_THEME)
                AudioManager.getInstance().playMusic(com.bismillahjuara.game.audio.AudioTrack.BATTLE_THEME, 2f);
            }
        };

        gameplayManager.onEnding1Cinematic = new Runnable() {
            @Override public void run() {
                hudManager.getDebugUI().playCinematic(
                    "ENDING 1",
                    "Timun akhirnya mengetahui bahwa sosok yang selama ini dibencinya ternyata adalah ayahnya sendiri.\n\nButo Ijo tidak pernah berniat membunuhnya. Namun semuanya sudah terlambat...",
                    7f,
                    new Runnable() { @Override public void run() { ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2f)); } }
                );
            }
        };

        gameplayManager.onEnding2Cinematic = new Runnable() {
            @Override public void run() {
                hudManager.getDebugUI().playCinematic(
                    "ENDING 2",
                    "Buto Ijo terbakar hingga lenyap. Barulah Timun mengetahui bahwa sosok tersebut adalah ayah kandungnya sendiri.\n\nDendam yang diwariskan ibunya ternyata berasal dari masa lalu yang jauh lebih kelam...",
                    7f,
                    new Runnable() { @Override public void run() { ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2f)); } }
                );
            }
        };

        Gdx.app.log("PROFILE_GAMESCREEN", "Init UI selesai: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void setupJumpscareUI() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.8f, 0f, 0f, 0.6f));
        pixmap.fill();
        bloodyTexture = new Texture(pixmap);
        pixmap.dispose();

        jumpscareOverlay = new Image(bloodyTexture);
        jumpscareOverlay.setFillParent(true);
        jumpscareOverlay.setVisible(false);

        hudManager.getStage().addActor(jumpscareOverlay);
    }

    private void pauseGame() {
        gameplayManager.pauseGame();
        hudManager.showPauseMenu();
    }

    private void resumeGame() {
        gameplayManager.resumeGame();
        hudManager.hidePauseMenu();
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (hudManager != null) multiplexer.addProcessor(hudManager.getStage());
        if (inputHandler != null) multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        gameplayManager.startGameplay();
    }

    @Override
    public void render(float delta) {
        if (gameplayManager == null) return;

        if (gameplayManager.getContext().state == GameplayState.GAME_OVER) {
            handleJumpscareSequence(delta);
            return;
        }

        inputHandler.update(delta);

        boolean isMobilePauseClicked = (hudManager.getMobileControls() != null && hudManager.getMobileControls().isPauseClicked());

        if (inputHandler.getAction().pausePressed || isMobilePauseClicked) {
            inputHandler.getAction().pausePressed = false;

            if (gameplayManager.getContext().state == GameplayState.PLAYING) {
                pauseGame();
            } else if (gameplayManager.getContext().state == GameplayState.PAUSED) {
                resumeGame();
            }
        }

        gameplayManager.update(delta);
        gameplayManager.render(delta);
        hudManager.updateAndRender(gameplayManager.getContext());
    }

    private void handleJumpscareSequence(float delta) {
        if (!isJumpscareTriggered) {
            isJumpscareTriggered = true;
            AudioManager.getInstance().stopMusic(0f);
            AudioManager.getInstance().playSFX(AudioSFX.JUMPSCARE);
            jumpscareOverlay.setVisible(true);
        }

        jumpscareTimer += delta;
        jumpscareOverlay.getColor().a = com.badlogic.gdx.math.MathUtils.random(0.3f, 1.0f);

        if (jumpscareTimer > 2.0f) {
            ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2.0f));
        }

        gameplayManager.render(0f);
        hudManager.getStage().draw();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) camera.resize(width, height);
        if (hudManager != null) hudManager.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        if (gameplayManager != null) gameplayManager.dispose();
        if (hudManager != null) hudManager.dispose();
        if (bloodyTexture != null) bloodyTexture.dispose();
    }
}
