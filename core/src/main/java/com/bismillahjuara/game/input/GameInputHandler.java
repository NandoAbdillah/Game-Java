package com.bismillahjuara.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.hud.HudManager;

public class GameInputHandler implements InputProcessor {

    public static final boolean IS_MOBILE = (Gdx.app != null) &&
        (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
            Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS);

    private BaseInputController activeController;

    // FIX: Menerima AdvancedCameraSystem, bukan OrbitCamera
    public GameInputHandler(AdvancedCameraSystem camera, HudManager hudManager) {
        if (IS_MOBILE) {
            activeController = new MobileInputController(camera, hudManager);
        } else {
            activeController = new DesktopInputController(camera);
        }
    }

    public void update(float delta) {
        activeController.update(delta);
    }

    public InputAction getAction() {
        return activeController.getAction();
    }

    @Override public boolean keyDown(int keycode) { return activeController.keyDown(keycode); }
    @Override public boolean keyUp(int keycode) { return activeController.keyUp(keycode); }
    @Override public boolean keyTyped(char character) { return activeController.keyTyped(character); }
    @Override public boolean touchDown(int sx, int sy, int ptr, int btn) { return activeController.touchDown(sx, sy, ptr, btn); }
    @Override public boolean touchUp(int sx, int sy, int ptr, int btn) { return activeController.touchUp(sx, sy, ptr, btn); }
    @Override public boolean touchCancelled(int sx, int sy, int ptr, int btn) { return activeController.touchCancelled(sx, sy, ptr, btn); }
    @Override public boolean touchDragged(int sx, int sy, int ptr) { return activeController.touchDragged(sx, sy, ptr); }
    @Override public boolean mouseMoved(int sx, int sy) { return activeController.mouseMoved(sx, sy); }
    @Override public boolean scrolled(float amountX, float amountY) { return activeController.scrolled(amountX, amountY); }
}
