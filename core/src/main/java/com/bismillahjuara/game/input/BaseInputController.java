package com.bismillahjuara.game.input;

import com.badlogic.gdx.InputAdapter;
import com.bismillahjuara.game.camera.OrbitCamera;


public abstract class BaseInputController extends InputAdapter {
    protected OrbitCamera camera;
    protected InputAction action;

    public BaseInputController(OrbitCamera camera) {
        this.camera = camera;
        this.action = new InputAction();
    }

    /**
     * Wajib diimplementasikan oleh setiap platform untuk meng-update nilai InputAction.
     */
    public abstract void update(float delta);

    public InputAction getAction() {
        return action;
    }
}
