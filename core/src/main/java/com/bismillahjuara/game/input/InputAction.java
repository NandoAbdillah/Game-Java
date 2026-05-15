package com.bismillahjuara.game.input;

/**
 * Data class yang menampung semua "niat" (intent) dari pemain.
 * Class ini murni hanya data, tidak ada logika Gdx.input di sini.
 */
public class InputAction {
    // --- AXIS / MOVEMENT (Analog/WASD) ---
    public float moveX = 0f;
    public float moveY = 0f;

    // --- HELD ACTIONS (Aksi yang ditahan) ---
    public boolean sprintHeld = false;
    public boolean crouchHeld = false;
    public boolean blockHeld = false;

    // --- TRANSIENT ACTIONS (Aksi sekali tekan / Trigger) ---
    public boolean jumpPressed = false;
    public boolean attackPressed = false;
    public boolean kickPressed = false;
    public boolean throwPressed = false;
    public boolean healPressed = false;
    public boolean emotePressed = false;
    public boolean crouchToggled = false;
    public boolean diePressed = false; // Untuk testing

    /**
     * Memastikan semua aksi trigger di-reset setiap frame agar tidak spam.
     * (LibGDX's isKeyJustPressed sudah otomatis, tapi method ini berguna untuk Mobile/Gamepad nanti)
     */
    public void resetTransientActions() {
        jumpPressed = false;
        attackPressed = false;
        kickPressed = false;
        throwPressed = false;
        healPressed = false;
        emotePressed = false;
        crouchToggled = false;
        diePressed = false;
    }
}
