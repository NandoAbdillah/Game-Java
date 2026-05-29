package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameplayManager;
import com.bismillahjuara.game.core.GameplayState;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.transitions.FadeTransition;

public class GameScreen implements Screen {

    private GameplayManager gameplayManager;
    private HudManager hudManager;
    private GameInputHandler inputHandler;
    private AdvancedCameraSystem camera;

    public GameScreen() {}

    public void initWorld() {
        gameplayManager = new GameplayManager();
        gameplayManager.buildWorld();
    }

    public void initEntities() {
        camera = new AdvancedCameraSystem();
        gameplayManager.buildEntities(camera);
    }

    public void initUI() {
        hudManager = new HudManager();
        inputHandler = new GameInputHandler(camera, hudManager);
        gameplayManager.bindInput(inputHandler);

        hudManager.getPauseMenuUI().onResumeCallback = new Runnable() {
            @Override public void run() { resumeGame(); }
        };

        hudManager.getPauseMenuUI().onMainMenuCallback = new Runnable() {
            @Override public void run() {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(1f));
            }
        };

        hudManager.getPauseMenuUI().onExitGameCallback = new Runnable() {
            @Override public void run() { Gdx.app.exit(); }
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

        // =========================================================
        // FIX BUG: UPDATE INPUT HARUS DI ATAS!
        // Agar status ESC yang ditekan segera dikonsumsi dan tidak ketinggalan 1 frame.
        // =========================================================
        inputHandler.update(delta);

        // CEK INTENT PAUSE (Dari Keyboard ESC atau Mobile Button)
        boolean isMobilePauseClicked = (hudManager.getMobileControls() != null && hudManager.getMobileControls().isPauseClicked());

        if (inputHandler.getAction().pausePressed || isMobilePauseClicked) {
            inputHandler.getAction().pausePressed = false; // Langsung Hapus/Konsumsi Event

            if (gameplayManager.getContext().state == GameplayState.PLAYING) {
                pauseGame();
            } else if (gameplayManager.getContext().state == GameplayState.PAUSED) {
                resumeGame();
            }
        }

        // UPDATE LOGIC
        gameplayManager.update(delta);

        // RENDER 3D & UI
        gameplayManager.render(delta);
        hudManager.updateAndRender(com.badlogic.gdx.math.Vector3.Zero, camera.getYaw());
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
    }
}
