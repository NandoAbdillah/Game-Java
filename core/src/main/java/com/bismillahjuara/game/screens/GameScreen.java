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
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.managers.GameplayManager;
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

    // =========================================================================
    // AAA LOADING EXPOSURE UNTUK ASYNC LOADER
    // =========================================================================
    public GameplayManager getGameplayManager() {
        return gameplayManager;
    }

    public void initCamera() {
        camera = new AdvancedCameraSystem();
    }

    // Tahap inisialisasi dipecah untuk Loader
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
        Gdx.app.log("PROFILE_GAMESCREEN", "Init UI selesai: " + (System.currentTimeMillis() - startTime) + " ms");
    }
    // =========================================================================

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
        hudManager.updateAndRender(gameplayManager.getContext().player.getPosition(), camera.getYaw());
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
