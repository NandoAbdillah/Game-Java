package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioTrack;
import com.bismillahjuara.game.managers.GameplayManager;
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

public class GameScreen implements Screen {

    private GameplayManager gameplayManager;
    private HudManager hudManager;
    private GameInputHandler inputHandler;
    private AdvancedCameraSystem camera;

    // jumpscare
    private boolean isJumpscareTriggered = false;
    private boolean isVideoFinished = false;
    private boolean isGameOverCinematicPlaying = false;
    private VideoPlayer jumpscareVideo;
    private Music jumpscareAudio;

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

        gameplayManager.onAct1Cinematic = new Runnable() {
            @Override public void run() {

                hudManager.getDebugUI().playCinematic(
                    "ACT I : PUSAKA YANG HILANG",
                    "Bukan sihir cahaya, melainkan benda-benda ritual peninggalan ibunya yang terasa berat dan mistis tersebar di dalam hutan.\n\nTimun harus menemukan seluruh pusaka tersebut jika ingin memiliki kekuatan untuk menghadapi Buto Ijo.",
                    15f,
                    new Runnable() {
                        @Override public void run() {
                            gameplayManager.resumeGame();

                            AudioManager.getInstance().stopMusic(1f);
                            AudioManager.getInstance().playAmbient(AudioTrack.FOREST_AMBIENT);
                        }
                    }
                );
                // putar backsound
                AudioManager.getInstance().playMusic(AudioTrack.THEME, 2f);
            }
        };

        gameplayManager.onAct2Cinematic = new Runnable() {
            @Override public void run() {
                gameplayManager.pauseGame();
                hudManager.getDebugUI().playCinematic(
                    "ACT II : TIMUN REVENGE'S",
                    "Ibunya berpesan bahwa pusaka-pusaka tersebut menyimpan kekuatan yang cukup untuk membalas dendam.\n\nButo Ijo harus dihentikan sebelum semuanya terlambat. Lemparkan pusaka timun sebanyak 10 kali untuk mengalahkannya.",
                    15f,
                    new Runnable() {
                        @Override public void run() {
                            gameplayManager.spawnButoIjo();
                            gameplayManager.resumeGame();

                            AudioManager.getInstance().stopMusic(1f);
                            AudioManager.getInstance().playMusic(AudioTrack.BOSS_THEME, 1f);
                        }
                    }
                );
                AudioManager.getInstance().playMusic(AudioTrack.THEME, 2f);
            }
        };

        gameplayManager.onEnding1Cinematic = new Runnable() {
            @Override public void run() {
                hudManager.getDebugUI().playCinematic(
                    "ENDING 1",
                    "Timun akhirnya mengetahui bahwa sosok yang selama ini dibencinya ternyata adalah ayahnya sendiri.\n\nButo Ijo tidak pernah berniat membunuhnya. Namun semuanya sudah terlambat...",
                    10f,
                    new Runnable() { @Override public void run() { ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2f)); } }
                );
            }
        };

        gameplayManager.onEnding2Cinematic = new Runnable() {
            @Override public void run() {

                hudManager.getDebugUI().playCinematic(
                    "ENDING 2",
                    "Buto Ijo terbakar hingga lenyap. Barulah Timun mengetahui bahwa sosok tersebut adalah ayah kandungnya sendiri.\n\nDendam yang diwariskan ibunya ternyata berasal dari masa lalu yang jauh lebih kelam...",
                    10f,
                    new Runnable() { @Override public void run() { ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2f)); } }
                );
            }
        };
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
            AudioManager.getInstance().stopAmbient();

            hudManager.getDebugUI().hideHUD();

            try {
                jumpscareVideo = VideoPlayerCreator.createVideoPlayer();
                jumpscareVideo.load(Gdx.files.internal("video/jumpscare.webm"));
                jumpscareVideo.setLooping(false);
                jumpscareVideo.setVolume(0f);
                jumpscareVideo.setOnCompletionListener(file -> {
                    Gdx.app.postRunnable(() -> {
                        isVideoFinished = true;
                    });
                });
                jumpscareVideo.play();

                jumpscareAudio = Gdx.audio.newMusic(Gdx.files.internal("video/jumpscare.ogg"));
                jumpscareAudio.play();

            } catch (Exception e) {
                Gdx.app.error("JUMPSCARE", "Gagal play video jumpscare", e);
                isVideoFinished = true;
            }
        }

        if (jumpscareVideo != null && !isVideoFinished) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            jumpscareVideo.update();
            Texture frame = jumpscareVideo.getTexture();
            if (frame != null) {
                SpriteBatch batch = (SpriteBatch) hudManager.getStage().getBatch();
                batch.begin();

                batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

                batch.draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                batch.end();
            }
        }


        else if (isVideoFinished && !isGameOverCinematicPlaying) {
            isGameOverCinematicPlaying = true;

            if (jumpscareVideo != null) { jumpscareVideo.dispose(); jumpscareVideo = null; }
            if (jumpscareAudio != null) { jumpscareAudio.dispose(); jumpscareAudio = null; }

            // ui game over
            hudManager.getDebugUI().playCinematic(
                "GAME OVER",
                "Timun menemukan fakta bahwa Sukma Gowong adalah entitas gaib yang mengambil jiwa seseorang karena ia dalah hasil dari praktik ilmu hitam yang gagal !",
                10f,
                () -> {
                    ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(2f));
                }
            );
        }

        if (isGameOverCinematicPlaying) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            hudManager.updateAndRender(gameplayManager.getContext());
        }
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
        if (hudManager != null) hudManager.dispose();
        if (jumpscareVideo != null) jumpscareVideo.dispose();
        if (jumpscareAudio != null) jumpscareAudio.dispose();

        if (gameplayManager != null && gameplayManager.getContext() != null) {
            if (gameplayManager.getContext().entityManager != null) {
                gameplayManager.getContext().entityManager.dispose();
            }
        }
    }
}
