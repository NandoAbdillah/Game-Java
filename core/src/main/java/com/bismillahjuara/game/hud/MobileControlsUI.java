package com.bismillahjuara.game.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;

/**
 * Layout kontrol sentuh Mobile.
 * Kiri: Joystick Super Besar. Kanan: Cluster Tombol Aksi (Jump, Attack, dll).
 */
public class MobileControlsUI {
    private Table mainTable;
    private Touchpad joystick;

    // Tombol Aksi
    private TextButton btnAttack;
    private TextButton btnJump;
    private TextButton btnThrow;
    private TextButton btnCrouch;

    public MobileControlsUI(HudAssets assets) {
        mainTable = new Table();
        mainTable.setFillParent(true);

        // KIRI: JOYSTICK
        // Deadzone 20f agar tidak terlalu sensitif kesenggol
        joystick = new Touchpad(20f, assets.skin, "default");

        // KANAN: CLUSTER TOMBOL AKSI
        Table actionTable = new Table();

        btnAttack = new TextButton("ATK", assets.skin, "default");
        btnJump = new TextButton("JUMP", assets.skin, "default");
        btnThrow = new TextButton("THROW", assets.skin, "default");
        btnCrouch = new TextButton("CROUCH", assets.skin, "default");

        // Layout
        // Baris 1: Tombol Throw & Jump di atas
        actionTable.add(btnThrow).size(100, 100).pad(10);
        actionTable.add(btnJump).size(120, 120).pad(10).padBottom(40).row();

        // Baris 2: Tombol Crouch & Attack di bawah
        actionTable.add(btnCrouch).size(100, 100).pad(10);
        actionTable.add(btnAttack).size(160, 160).pad(10); // Attack paling besar

        // SUSUN KE LAYAR UTAMA
        mainTable.bottom().left();
        // Joystick memakan ruang sebelah kiri dan didorong ke pojok bawah
        mainTable.add(joystick).size(350, 350).pad(50).expandX().left();
        // Tombol aksi memakan ruang sebelah kanan dan didorong ke pojok bawah
        mainTable.add(actionTable).pad(40).expandX().right();
    }

    public Table getRootTable() {
        return mainTable;
    }

    // GETTER UNTUK MOBILE INPUT CONTROLLER
    public float getJoystickX() { return joystick.getKnobPercentX(); }
    public float getJoystickY() { return joystick.getKnobPercentY(); }

    public boolean isAttackPressed() { return btnAttack.isPressed(); }
    public boolean isJumpPressed() { return btnJump.isPressed(); }
    public boolean isThrowPressed() { return btnThrow.isPressed(); }
    public boolean isCrouchPressed() { return btnCrouch.isPressed(); }
}
