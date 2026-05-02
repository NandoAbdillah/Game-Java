package com.bismillahjuara.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.bismillahjuara.game.camera.OrbitCamera;

public class GameInputHandler implements InputProcessor {

    public static final boolean IS_MOBILE = (Gdx.app != null) &&
        (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
            Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS);

    private OrbitCamera camera;

    // Konstanta Input
    public static final float JOYSTICK_RADIUS  = 80f;
    private static final float JOYSTICK_ZONE_W  = 0.45f;
    private static final float CAM_ROTATE_SPEED = 0.3f;
    private static final float CAM_ZOOM_SPEED   = 1.5f;

    // State PC
    private boolean isDragging = false;
    private int lastDragX, lastDragY;

    // State Mobile
    public boolean joystickActive = false;
    public int joystickPointer = -1;
    public Vector2 joystickCenter = new Vector2();
    public Vector2 joystickCurrent = new Vector2();

    public boolean camTouchActive = false;
    public int camTouchPointer = -1;
    private float camTouchLastX, camTouchLastY;

    private boolean pinching = false;
    private float pinchLastDist = 0f;

    public GameInputHandler(OrbitCamera camera) {
        this.camera = camera;
    }

    /**
     * Membaca tombol WASD atau pergeseran Joystick untuk dikirim ke karakter
     */
    public Vector2 getMoveInput() {
        Vector2 moveInput = new Vector2();

        if (!IS_MOBILE) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveInput.y += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveInput.y -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveInput.x -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveInput.x += 1;
        } else {
            if (joystickActive) {
                Vector2 jDelta = new Vector2(joystickCurrent).sub(joystickCenter);
                float jLen = jDelta.len();
                if (jLen > JOYSTICK_RADIUS) jDelta.nor().scl(JOYSTICK_RADIUS);
                moveInput.set(jDelta.x / JOYSTICK_RADIUS, jDelta.y / JOYSTICK_RADIUS);
            }
        }
        return moveInput;
    }

    public boolean isSprinting() {
        return !IS_MOBILE && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
    }

    // --- IMPLEMENTASI INPUT PROCESSOR ---
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int sx, int sy, int pointer, int button) {
        if (!IS_MOBILE) {
            if (button == Input.Buttons.LEFT) {
                isDragging = true; lastDragX = sx; lastDragY = sy;
                return true;
            }
        } else {
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();

            if (joystickActive && pointer != joystickPointer && sx > screenW * JOYSTICK_ZONE_W) {
                if (camTouchActive) {
                    pinching = true;
                    pinchLastDist = Vector2.dst(camTouchLastX, camTouchLastY, sx, sy);
                    return true;
                }
            }

            if (sx < screenW * JOYSTICK_ZONE_W && !joystickActive) {
                joystickActive = true; joystickPointer = pointer;
                joystickCenter.set(sx, screenH - sy); joystickCurrent.set(sx, screenH - sy);
                return true;
            } else if (sx >= screenW * JOYSTICK_ZONE_W && !camTouchActive) {
                camTouchActive = true; camTouchPointer = pointer;
                camTouchLastX = sx; camTouchLastY = sy;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int sx, int sy, int pointer, int button) {
        if (!IS_MOBILE) {
            if (button == Input.Buttons.LEFT) { isDragging = false; return true; }
        } else {
            if (pointer == joystickPointer) { joystickActive = false; joystickPointer = -1; joystickCurrent.set(joystickCenter); }
            if (pointer == camTouchPointer) { camTouchActive = false; camTouchPointer = -1; pinching = false; }
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int sx, int sy, int pointer) {
        if (!IS_MOBILE) {
            if (isDragging) {
                camera.addTargetYaw(-(sx - lastDragX) * CAM_ROTATE_SPEED);
                camera.addTargetPitch((sy - lastDragY) * CAM_ROTATE_SPEED);
                lastDragX = sx; lastDragY = sy;
                return true;
            }
        } else {
            float screenH = Gdx.graphics.getHeight();
            if (pinching) {
                if (pointer == camTouchPointer) {
                    float newDist = Vector2.dst(camTouchLastX, (screenH - camTouchLastY), sx, (screenH - (float)sy));
                    camera.setPinchDist(camera.getTargetDist() + (pinchLastDist - newDist) * 0.05f);
                    pinchLastDist = newDist;
                }
                return true;
            }
            if (pointer == joystickPointer && joystickActive) { joystickCurrent.set(sx, screenH - sy); return true; }
            if (pointer == camTouchPointer && camTouchActive) {
                camera.addTargetYaw(-(sx - camTouchLastX) * CAM_ROTATE_SPEED);
                camera.addTargetPitch((sy - camTouchLastY) * CAM_ROTATE_SPEED);
                camTouchLastX = sx; camTouchLastY = sy;
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseMoved(int sx, int sy) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) {
        camera.addTargetDist(amountY * CAM_ZOOM_SPEED);
        return true;
    }
}
