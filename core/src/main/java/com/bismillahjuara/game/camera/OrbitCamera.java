package com.bismillahjuara.game.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class OrbitCamera {

    private PerspectiveCamera cam;

    // Constants
    private static final float CAM_DISTANCE_DEFAULT = 10f;
    public static final float CAM_DISTANCE_MIN     = 3f;
    public static final float CAM_DISTANCE_MAX     = 25f;
    public static final float CAM_PITCH_MIN        = 5f;
    public static final float CAM_PITCH_MAX        = 80f;
    private static final float CAM_SMOOTH           = 8f;

    // State
    private float camYaw       = 0f;
    private float camPitch     = 30f;
    private float camDistance  = CAM_DISTANCE_DEFAULT;

    // Target smooth
    private float targetYaw    = 0f;
    private float targetPitch  = 30f;
    private float targetDist   = CAM_DISTANCE_DEFAULT;

    public OrbitCamera() {
        cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.2f;
        cam.far  = 200f;
    }

    public void update(Vector3 targetPos, float delta) {
        // Smooth lerping
        float t = Math.min(CAM_SMOOTH * delta, 1f);
        camYaw      = lerpAngle(camYaw, targetYaw, t);
        camPitch    += (targetPitch - camPitch) * t;
        camDistance += (targetDist  - camDistance) * t;

        // Hitung posisi
        float pitchRad = MathUtils.degreesToRadians * camPitch;
        float yawRad   = MathUtils.degreesToRadians * camYaw;

        float hDist = camDistance * MathUtils.cos(pitchRad);
        float vDist = camDistance * MathUtils.sin(pitchRad);

        float camX = targetPos.x + hDist * MathUtils.sin(yawRad);
        float camZ = targetPos.z + hDist * MathUtils.cos(yawRad);
        float camY = targetPos.y + 0.5f + vDist;

        cam.position.set(camX, camY, camZ);
        cam.lookAt(targetPos.x, targetPos.y + 0.5f, targetPos.z);
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

    // Getters & Setters untuk dimanipulasi oleh InputHandler
    public PerspectiveCamera getCam() { return cam; }
    public float getYaw() { return camYaw; }

    public void addTargetYaw(float deltaYaw) { this.targetYaw += deltaYaw; }
    public void addTargetPitch(float deltaPitch) {
        this.targetPitch += deltaPitch;
        this.targetPitch = MathUtils.clamp(this.targetPitch, CAM_PITCH_MIN, CAM_PITCH_MAX);
    }
    public void addTargetDist(float deltaDist) {
        this.targetDist += deltaDist;
        this.targetDist = MathUtils.clamp(this.targetDist, CAM_DISTANCE_MIN, CAM_DISTANCE_MAX);
    }
    public void setPinchDist(float dist) {
        this.targetDist = dist;
        this.targetDist = MathUtils.clamp(this.targetDist, CAM_DISTANCE_MIN, CAM_DISTANCE_MAX);
    }
    public float getTargetDist() { return targetDist; }
}
