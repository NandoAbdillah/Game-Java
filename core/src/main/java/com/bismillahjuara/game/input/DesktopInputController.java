package com.bismillahjuara.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;

public class DesktopInputController extends BaseInputController {

    private boolean isDragging = false;
    private int lastDragX, lastDragY;
    private static final float CAM_ROTATE_SPEED = 0.3f;
    private static final float CAM_ZOOM_SPEED   = 1.5f;

    public DesktopInputController(AdvancedCameraSystem camera) {
        super(camera);
    }

    @Override
    public void update(float delta) {
        float x = 0;
        float y = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1;

        Vector2 move = new Vector2(x, y);
        if (move.len2() > 0) move.nor();

        action.moveX = move.x;
        action.moveY = move.y;

        action.sprintHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        action.crouchHeld = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        action.jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        // FIX TAHAP 2: Ubah tombol serang ke Klik KIRI (Intuisi Game PC)
        action.attackPressed = Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        action.kickPressed = Gdx.input.isKeyJustPressed(Input.Keys.R);
        action.throwPressed = Gdx.input.isKeyJustPressed(Input.Keys.E);
        action.healPressed = Gdx.input.isKeyJustPressed(Input.Keys.H);
        action.emotePressed = Gdx.input.isKeyJustPressed(Input.Keys.T);
        action.crouchToggled = Gdx.input.isKeyJustPressed(Input.Keys.C);
        action.diePressed = Gdx.input.isKeyJustPressed(Input.Keys.K);
        action.toggleCameraPressed = Gdx.input.isKeyJustPressed(Input.Keys.V);
        action.pausePressed = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // FIX TAHAP 2: Geser kamera pakai Klik KANAN
        if (button == Input.Buttons.RIGHT) {
            isDragging = true;
            lastDragX = screenX;
            lastDragY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.RIGHT) {
            isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isDragging) {
            camera.addTargetYaw(-(screenX - lastDragX) * CAM_ROTATE_SPEED);
            camera.addTargetPitch((screenY - lastDragY) * CAM_ROTATE_SPEED);
            lastDragX = screenX;
            lastDragY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.addZoom(amountY * CAM_ZOOM_SPEED);
        return true;
    }
}
