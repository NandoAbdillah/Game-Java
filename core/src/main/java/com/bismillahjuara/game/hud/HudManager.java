package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bismillahjuara.game.input.GameInputHandler;

/**
 * Manajer UI Modern menggunakan LibGDX Scene2D.
 * Menyimpan Stage dan merender semua layer HUD (Gameplay, Controls, Debug).
 */
public class HudManager {
    private Stage stage;
    private HudAssets assets;

    private MobileControlsUI mobileControls;
    private DebugUI debugUI;

    public HudManager() {
        // ScreenViewport memastikan UI ukurannya 1:1 dengan resolusi layar asli
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
    }

    public void updateAndRender(Vector3 playerPos, float camYaw) {
        // Update data statis/debug
        debugUI.update(playerPos, camYaw, GameInputHandler.IS_MOBILE);

        // Render Stage
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
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
    }
}
