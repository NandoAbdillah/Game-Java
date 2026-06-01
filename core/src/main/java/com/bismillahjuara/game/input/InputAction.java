package com.bismillahjuara.game.input;

public class InputAction {
    // MOVEMENT
    public float moveX = 0f;
    public float moveY = 0f;

    // ACTION
    public boolean sprintHeld = false;
    public boolean crouchHeld = false;
    public boolean blockHeld = false;

    public boolean jumpPressed = false;
    public boolean attackPressed = false;
    public boolean kickPressed = false;
    public boolean throwPressed = false;
    public boolean healPressed = false;
    public boolean emotePressed = false;
    public boolean crouchToggled = false;
    public boolean diePressed = false; // Untuk testing

    public boolean toggleCameraPressed = false;
    public boolean pausePressed = false;

    public void resetTransientActions() {
        jumpPressed = false;
        attackPressed = false;
        kickPressed = false;
        throwPressed = false;
        healPressed = false;
        emotePressed = false;
        crouchToggled = false;
        diePressed = false;
        pausePressed = false;
    }
}
