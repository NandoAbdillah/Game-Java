package com.bismillahjuara.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.hud.MobileControlsUI;
//inheritance
public class MobileInputController extends BaseInputController {

    private HudManager hudManager;
    private static final float CAM_ROTATE_SPEED = 0.3f;

    public boolean camTouchActive = false;
    public int camTouchPointer = -1;
    private float camTouchLastX, camTouchLastY;
    private boolean pinching = false;
    private float pinchLastDist = 0f;

    private boolean lastJumpState = false;
    private boolean lastAttackState = false;
    private boolean lastThrowState = false;
    private boolean lastCrouchState = false;

    public MobileInputController(AdvancedCameraSystem camera, HudManager hudManager) {
        super(camera);
        this.hudManager = hudManager;
    }

    @Override
    public void update(float delta) {
        if (hudManager != null && hudManager.getMobileControls() != null) {
            MobileControlsUI controls = hudManager.getMobileControls();

            action.moveX = controls.getJoystickX();
            action.moveY = controls.getJoystickY();

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

            float currentJoyLen = Math.max(Math.abs(action.moveX), Math.abs(action.moveY));
            if (currentJoyLen > 0.85f) {
                action.sprintHeld = true;  // Lari kencang
            } else if (currentJoyLen < 0.70f) {
                action.sprintHeld = false; // Turun jadi jalan kaki
            }


        } else {
            action.moveX = 0f;
            action.moveY = 0f;
            action.sprintHeld = false;
        }
    }

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

                float zoomDelta = (pinchLastDist - newDist) * 0.05f;
                camera.addZoom(zoomDelta);

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
