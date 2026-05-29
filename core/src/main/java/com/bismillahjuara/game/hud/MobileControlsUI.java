package com.bismillahjuara.game.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.bismillahjuara.game.ui.AnimatedImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

/**
 * Layout kontrol sentuh Mobile.
 */
public class MobileControlsUI {
    private Table mainTable;
    private Touchpad joystick;

    // Tombol Aksi
    private TextButton btnAttack;
    private TextButton btnJump;
    private TextButton btnThrow;
    private TextButton btnCrouch;

    // Tombol Pause Mobile
    private AnimatedImageButton btnPause;
    private boolean pauseClicked = false;

    public MobileControlsUI(HudAssets assets) {
        mainTable = new Table();
        mainTable.setFillParent(true);

        // --- KIRI: JOYSTICK ---
        // Deadzone 20f agar tidak terlalu sensitif kesenggol
        joystick = new Touchpad(20f, assets.skin, "default");

        // --- KANAN: CLUSTER TOMBOL AKSI ---
        Table actionTable = new Table();

        btnAttack = new TextButton("ATK", assets.skin, "default");
        btnJump = new TextButton("JUMP", assets.skin, "default");
        btnThrow = new TextButton("THROW", assets.skin, "default");
        btnCrouch = new TextButton("CROUCH", assets.skin, "default");

        // Layout ala Action RPG
        // Baris 1: Tombol Throw & Jump di atas
        actionTable.add(btnThrow).size(100, 100).pad(10);
        actionTable.add(btnJump).size(120, 120).pad(10).padBottom(40).row();

        // Baris 2: Tombol Crouch & Attack di bawah
        actionTable.add(btnCrouch).size(100, 100).pad(10);
        actionTable.add(btnAttack).size(160, 160).pad(10); // Attack paling besar

        // --- TOMBOL PAUSE (POJOK KANAN ATAS) ---
        if (assets.pauseBtnMobileTex != null) {
            btnPause = new AnimatedImageButton(assets.pauseBtnMobileTex);
            btnPause.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    pauseClicked = true; // Akan dibaca oleh GameScreen/InputHandler
                }
            });
        }

        // --- SUSUN KE LAYAR UTAMA ---
        mainTable.bottom().left();
        // Joystick memakan ruang sebelah kiri dan didorong ke pojok bawah
        mainTable.add(joystick).size(350, 350).pad(50).expandX().left();
        // Tombol aksi memakan ruang sebelah kanan dan didorong ke pojok bawah
        mainTable.add(actionTable).pad(40).expandX().right();

        // Taruh tombol pause terpisah di layer yang sama, pojok kanan atas
        if (btnPause != null) {
            Table topTable = new Table();
            topTable.setFillParent(true);
            topTable.top().right().pad(40);
            topTable.add(btnPause).size(80, 80);
            mainTable.addActor(topTable); // Pasang di atas main table
        }
    }

    public Table getRootTable() {
        return mainTable;
    }

    // --- GETTER UNTUK MOBILE INPUT CONTROLLER ---
    public float getJoystickX() { return joystick.getKnobPercentX(); }
    public float getJoystickY() { return joystick.getKnobPercentY(); }

    public boolean isAttackPressed() { return btnAttack.isPressed(); }
    public boolean isJumpPressed() { return btnJump.isPressed(); }
    public boolean isThrowPressed() { return btnThrow.isPressed(); }
    public boolean isCrouchPressed() { return btnCrouch.isPressed(); }

    // Ambil status pause, lalu reset (konsumsi event)
    public boolean isPauseClicked() {
        if (pauseClicked) {
            pauseClicked = false;
            return true;
        }
        return false;
    }
}
