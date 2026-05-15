package com.bismillahjuara.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.bismillahjuara.game.camera.OrbitCamera;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.hud.MobileControlsUI;

public class MobileInputController extends BaseInputController {

    private HudManager hudManager;
    private static final float CAM_ROTATE_SPEED = 0.3f;

    // Data Kamera Sentuh
    public boolean camTouchActive = false;
    public int camTouchPointer = -1;
    private float camTouchLastX, camTouchLastY;
    private boolean pinching = false;
    private float pinchLastDist = 0f;

    // Tracker status tombol sebelumnya (Untuk mensimulasikan isKeyJustPressed di Mobile)
    private boolean lastJumpState = false;
    private boolean lastAttackState = false;
    private boolean lastThrowState = false;
    private boolean lastCrouchState = false;

    public MobileInputController(OrbitCamera camera, HudManager hudManager) {
        super(camera);
        this.hudManager = hudManager;
    }

    @Override
    public void update(float delta) {
        if (hudManager != null && hudManager.getMobileControls() != null) {
            MobileControlsUI controls = hudManager.getMobileControls();

            // 1. Update Joystick Analog
            action.moveX = controls.getJoystickX();
            action.moveY = controls.getJoystickY();

            // 2. Baca Tombol Aksi & Deteksi Trigger Sekali Pencet (Edge Detection)
            boolean currentJump = controls.isJumpPressed();
            action.jumpPressed = currentJump && !lastJumpState;
            lastJumpState = currentJump;

            boolean currentAttack = controls.isAttackPressed();
            action.attackPressed = currentAttack && !lastAttackState;
            lastAttackState = currentAttack;

            boolean currentThrow = controls.isThrowPressed();
            action.throwPressed = currentThrow && !lastThrowState;
            lastThrowState = currentThrow;

            boolean currentCrouch = controls.isCrouchPressed();
            action.crouchToggled = currentCrouch && !lastCrouchState;
            lastCrouchState = currentCrouch;

            // Fitur Sprint Khusus Mobile: Kalau ditarik pol, otomatis sprint!
            action.sprintHeld = (Math.abs(action.moveX) > 0.8f || Math.abs(action.moveY) > 0.8f);

        } else {
            action.moveX = 0f;
            action.moveY = 0f;
            action.sprintHeld = false;
        }
    }

    // --- KONTROL KAMERA (Area sisa yang tidak terhalang UI) ---
    @Override
    public boolean touchDown(int sx, int sy, int pointer, int button) {
        if (!camTouchActive) {
            camTouchActive = true;
            camTouchPointer = pointer;
            camTouchLastX = sx;
            camTouchLastY = sy;
            return true;
        } else if (camTouchActive && pointer != camTouchPointer) {
            pinching = true;
            pinchLastDist = Vector2.dst(camTouchLastX, camTouchLastY, sx, sy);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int sx, int sy, int pointer, int button) {
        if (pointer == camTouchPointer) {
            camTouchActive = false;
            camTouchPointer = -1;
            pinching = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int sx, int sy, int pointer) {
        float screenH = Gdx.graphics.getHeight();
        if (pinching) {
            if (pointer == camTouchPointer) {
                float newDist = Vector2.dst(camTouchLastX, (screenH - camTouchLastY), sx, (screenH - (float)sy));
                camera.setPinchDist(camera.getTargetDist() + (pinchLastDist - newDist) * 0.05f);
                pinchLastDist = newDist;
            }
            return true;
        }
        if (pointer == camTouchPointer && camTouchActive) {
            camera.addTargetYaw(-(sx - camTouchLastX) * CAM_ROTATE_SPEED);
            camera.addTargetPitch((sy - camTouchLastY) * CAM_ROTATE_SPEED);
            camTouchLastX = sx;
            camTouchLastY = sy;
            return true;
        }
        return false;
    }
}
