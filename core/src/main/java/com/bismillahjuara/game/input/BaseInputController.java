package com.bismillahjuara.game.input;

import com.badlogic.gdx.InputAdapter;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
//abstraksi,inheritance,
public abstract class BaseInputController extends InputAdapter {
    protected AdvancedCameraSystem camera;
    protected InputAction action;

    public BaseInputController(AdvancedCameraSystem camera) {
        this.camera = camera;
        this.action = new InputAction();
    }

    public abstract void update(float delta);

    public InputAction getAction() {
        return action;
    }
}
