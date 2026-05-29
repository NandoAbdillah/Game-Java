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

        // --- WIRE CALLBACKS PAUSE MENU ---
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
        // Lepas input gameplay agar player tidak bisa kontrol karakter di belakang layar,
        // pastikan Input UI tetap nyala. InputMultiplexer di method show() mengurus ini karena Stage ada di urutan pertama.
    }

    private void resumeGame() {
        gameplayManager.resumeGame();
        hudManager.hidePauseMenu();
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        // Urutan PENTING: Stage (UI) pertama, lalu Input Gameplay
        if (hudManager != null) multiplexer.addProcessor(hudManager.getStage());
        if (inputHandler != null) multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        gameplayManager.startGameplay();
    }

    @Override
    public void render(float delta) {
        if (gameplayManager == null) return;

        // 1. CEK INTENT PAUSE (Dari Keyboard ESC atau Mobile Button)
        boolean isMobilePauseClicked = (hudManager.getMobileControls() != null && hudManager.getMobileControls().isPauseClicked());

        if (inputHandler.getAction().pausePressed || isMobilePauseClicked) {
            inputHandler.getAction().pausePressed = false; // Consume event

            if (gameplayManager.getContext().state == GameplayState.PLAYING) {
                pauseGame();
            } else if (gameplayManager.getContext().state == GameplayState.PAUSED) {
                resumeGame(); // Toggle mati jika ditekan lagi
            }
        }

        // 2. UPDATE LOGIC (UpdatePipeline otomatis mengabaikan update entity jika state = PAUSED)
        inputHandler.update(delta);
        gameplayManager.update(delta);

        // 3. RENDER 3D (RenderPipeline otomatis membekukan animasi jika state = PAUSED)
        gameplayManager.render(delta);

        // 4. RENDER UI (UI Selalu berjalan normal agar tombol animasi jalan)
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
