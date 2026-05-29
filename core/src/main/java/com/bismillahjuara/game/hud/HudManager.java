package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bismillahjuara.game.input.GameInputHandler;

/**
 * Manajer UI Modern.
 */
public class HudManager {
    private Stage stage;
    private HudAssets assets;

    private MobileControlsUI mobileControls;
    private DebugUI debugUI;
    private PauseMenuUI pauseMenuUI; // Tambahan

    public HudManager() {
        stage = new Stage(new ScreenViewport());
        assets = new HudAssets();

        // 1. Buat Debug UI
        debugUI = new DebugUI(assets);
        stage.addActor(debugUI.getRootTable());

        // 2. Buat Mobile Controls jika di HP
        if (GameInputHandler.IS_MOBILE) {
            mobileControls = new MobileControlsUI(assets);
            stage.addActor(mobileControls.getRootTable());
        }

        // 3. Buat Pause Menu UI (Paling atas)
        pauseMenuUI = new PauseMenuUI(assets);
        stage.addActor(pauseMenuUI.getRootTable());
    }

    public void updateAndRender(Vector3 playerPos, float camYaw) {
        debugUI.update(playerPos, camYaw, GameInputHandler.IS_MOBILE);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // --- PAUSE API ---
    public void showPauseMenu() {
        if (!pauseMenuUI.isVisible()) pauseMenuUI.show();
        if (mobileControls != null) mobileControls.getRootTable().setVisible(false); // Sembunyikan joystick
    }

    public void hidePauseMenu() {
        if (pauseMenuUI.isVisible()) pauseMenuUI.hide();
        if (mobileControls != null) mobileControls.getRootTable().setVisible(true); // Munculkan joystick
    }

    public PauseMenuUI getPauseMenuUI() {
        return pauseMenuUI;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /** Digunakan oleh InputMultiplexer */
    public Stage getStage() {
        return stage;
    }

    /** Digunakan oleh MobileInputController untuk membaca pergerakan Joystick */
    public MobileControlsUI getMobileControls() {
        return mobileControls;
    }

    public void dispose() {
        stage.dispose();
        assets.dispose();
        if (pauseMenuUI != null) pauseMenuUI.dispose();
    }
}
