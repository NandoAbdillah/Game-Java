package com.bismillahjuara.game.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class AdvancedCameraSystem {

    public enum CameraMode {
        THIRD_PERSON, FIRST_PERSON
    }

    private PerspectiveCamera cam;
    private CameraMode currentMode = CameraMode.THIRD_PERSON;

    //  JARAK & OFFSET
    private float targetTpsDistance = 12f;
    private static final float MIN_TPS_DIST = 4f;
    private static final float MAX_TPS_DIST = 25f;

    private static final float FPS_DISTANCE = 0.5f;
    private static final float TPS_HEIGHT_OFFSET = 2.5f;
    private static final float FPS_HEIGHT_OFFSET = 3.5f;
    private static final float TRANSITION_SPEED = 6f;


    // STATE KAMERA
    private float currentDistance = 12f;
    private float currentHeightOffset = TPS_HEIGHT_OFFSET;

    private float camYaw = 0f;
    private float camPitch = 20f;
    private float targetYaw = 0f;
    private float targetPitch = 20f;

    public static final float CAM_PITCH_MIN = -10f;
    public static final float CAM_PITCH_MAX = 80f;

    public AdvancedCameraSystem() {
        cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far  = 300f;
    }

    public void toggleMode() {
        if (currentMode == CameraMode.THIRD_PERSON) {
            currentMode = CameraMode.FIRST_PERSON;
        } else {
            currentMode = CameraMode.THIRD_PERSON;
        }
    }

    public void update(Vector3 targetPos, float delta) {
        float targetDist = (currentMode == CameraMode.THIRD_PERSON) ? targetTpsDistance : FPS_DISTANCE;
        float targetHeight = (currentMode == CameraMode.THIRD_PERSON) ? TPS_HEIGHT_OFFSET : FPS_HEIGHT_OFFSET;

        float lerpFactor = Math.min(TRANSITION_SPEED * delta, 1f);
        currentDistance = MathUtils.lerp(currentDistance, targetDist, lerpFactor);
        currentHeightOffset = MathUtils.lerp(currentHeightOffset, targetHeight, lerpFactor);

        camYaw = lerpAngle(camYaw, targetYaw, lerpFactor * 2f);
        camPitch += (targetPitch - camPitch) * lerpFactor * 2f;

        float pitchRad = MathUtils.degreesToRadians * camPitch;
        float yawRad   = MathUtils.degreesToRadians * camYaw;

        float hDist = currentDistance * MathUtils.cos(pitchRad);
        float vDist = currentDistance * MathUtils.sin(pitchRad);

        float camX = targetPos.x + hDist * MathUtils.sin(yawRad);
        float camZ = targetPos.z + hDist * MathUtils.cos(yawRad);
        float camY = targetPos.y + currentHeightOffset + vDist;

        cam.position.set(camX, camY, camZ);
        cam.lookAt(targetPos.x, targetPos.y + currentHeightOffset, targetPos.z);
        cam.up.set(Vector3.Y);
        cam.update();
    }

    private float lerpAngle(float a, float b, float t) {
        float diff = b - a;
        while (diff > 180)  diff -= 360;
        while (diff < -180) diff += 360;
        return a + diff * t;
    }

    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    public PerspectiveCamera getCam() { return cam; }
    public float getYaw() { return camYaw; }
    public CameraMode getCurrentMode() { return currentMode; }

    public void addTargetYaw(float deltaYaw) { this.targetYaw += deltaYaw; }
    public void addTargetPitch(float deltaPitch) {
        this.targetPitch = MathUtils.clamp(this.targetPitch + deltaPitch, CAM_PITCH_MIN, CAM_PITCH_MAX);
    }

    // FUNGSI ZOOM SCROLL / PINCH
    public void addZoom(float amount) {
        if (currentMode == CameraMode.THIRD_PERSON) {
            targetTpsDistance = MathUtils.clamp(targetTpsDistance + amount, MIN_TPS_DIST, MAX_TPS_DIST);
        }
    }
}
