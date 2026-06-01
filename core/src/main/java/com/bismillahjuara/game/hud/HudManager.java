package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.input.GameInputHandler;

public class HudManager {
    private Stage stage;
    private HudAssets assets;

    private MobileControlsUI mobileControls;
    private DebugUI debugUI;
    private PauseMenuUI pauseMenuUI;

    public HudManager() {
        stage = new Stage(new ScreenViewport());
        assets = new HudAssets();

        // Buat Debug UI
        debugUI = new DebugUI();
        stage.addActor(debugUI.getRootTable());

        // Buat Mobile Controls jika di HP
        if (GameInputHandler.IS_MOBILE) {
            mobileControls = new MobileControlsUI(assets);
            stage.addActor(mobileControls.getRootTable());
        }

        // Buat Pause Menu UI (Paling atas)
        pauseMenuUI = new PauseMenuUI(assets);
        stage.addActor(pauseMenuUI.getRootTable());
    }

    public void updateAndRender(GameContext context) {
        // Buka kunci update DebugUI!
        if (context != null) {
            debugUI.update(Gdx.graphics.getDeltaTime(), context);
        }

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

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

    public DebugUI getDebugUI() {
        return debugUI;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return stage;
    }

    public MobileControlsUI getMobileControls() {
        return mobileControls;
    }

    public void dispose() {
        stage.dispose();
        assets.dispose();
        if (pauseMenuUI != null) pauseMenuUI.dispose();
    }
}
