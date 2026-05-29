package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bismillahjuara.game.ui.AnimatedImageButton;

/**
 * AAA Production Pause Menu.
 * Menangani state visual Resume, Settings Overlay, dan Confirmation.
 */
public class PauseMenuUI {

    private Table rootTable;
    private Table mainButtonsTable;
    private Table confirmTable;
    private Table settingsOverlayTable;

    private Texture bgTexture;

    // Callback interfaces untuk menghubungkan UI dengan GameScreen
    public Runnable onResumeCallback;
    public Runnable onMainMenuCallback;
    public Runnable onExitGameCallback;

    public PauseMenuUI(HudAssets assets) {
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setVisible(false); // Sembunyikan secara default

        // 1. Buat Dark Overlay Background (Hitam transparan 75%)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.75f);
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        pixmap.dispose();

        Image bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        rootTable.addActor(bgImage);

        // 2. Setup Struktur Menu
        setupMainButtons(assets);
        setupConfirmDialog(assets);
        setupSettingsOverlay(assets);

        showMainMenu(); // Tampilkan tombol utama, sembunyikan dialog
    }

    private void setupMainButtons(HudAssets assets) {
        mainButtonsTable = new Table();
        mainButtonsTable.setFillParent(true);

        // Title
        if (assets.pauseTitleTex != null) {
            Image title = new Image(assets.pauseTitleTex);
            mainButtonsTable.add(title).size(400, 120).padBottom(60).row();
        }

        // Buttons
        if (assets.resumeTex != null) {
            AnimatedImageButton btn = new AnimatedImageButton(assets.resumeTex);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    if (onResumeCallback != null) onResumeCallback.run();
                }
            });
            mainButtonsTable.add(btn).size(300, 80).padBottom(15).row();
        }

        if (assets.settingsTex != null) {
            AnimatedImageButton btn = new AnimatedImageButton(assets.settingsTex);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    showSettingsOverlay();
                }
            });
            mainButtonsTable.add(btn).size(300, 80).padBottom(15).row();
        }

        if (assets.mainMenuTex != null) {
            AnimatedImageButton btn = new AnimatedImageButton(assets.mainMenuTex);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    showConfirm("Return to Main Menu?", onMainMenuCallback);
                }
            });
            mainButtonsTable.add(btn).size(300, 80).padBottom(15).row();
        }

        if (assets.closeGameTex != null) {
            AnimatedImageButton btn = new AnimatedImageButton(assets.closeGameTex);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    // Di AAA Game PC kadang langsung quit, tapi di mobile konfirmasi. Kita pakai konfirmasi untuk keduanya agar aman.
                    showConfirm("Exit Nightmare to Desktop?", onExitGameCallback);
                }
            });
            mainButtonsTable.add(btn).size(300, 80).row();
        }

        rootTable.addActor(mainButtonsTable);
    }

    private void setupConfirmDialog(HudAssets assets) {
        confirmTable = new Table();
        confirmTable.setFillParent(true);

        Label.LabelStyle style = new Label.LabelStyle(assets.defaultFont, Color.WHITE);
        Label questionLabel = new Label("Are you sure?", style);

        // Pakai teks biasa sementara untuk Yes/No, kecuali ada PNG-nya
        Label.LabelStyle btnStyle = new Label.LabelStyle(assets.defaultFont, Color.RED);
        Label btnYes = new Label("[ YES ]", btnStyle);
        Label btnNo = new Label("[ CANCEL ]", style);

        // Efek Hover Label biasa
        btnYes.addListener(new ClickListener() {
            Runnable action;
            @Override public void clicked(InputEvent e, float x, float y) {
                if (confirmTable.getUserObject() instanceof Runnable) {
                    ((Runnable) confirmTable.getUserObject()).run();
                }
            }
        });

        btnNo.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                showMainMenu();
            }
        });

        confirmTable.add(questionLabel).colspan(2).padBottom(40).row();
        confirmTable.add(btnYes).padRight(50);
        confirmTable.add(btnNo);

        rootTable.addActor(confirmTable);
    }

    private void setupSettingsOverlay(HudAssets assets) {
        settingsOverlayTable = new Table();
        settingsOverlayTable.setFillParent(true);

        Label.LabelStyle style = new Label.LabelStyle(assets.defaultFont, Color.WHITE);
        settingsOverlayTable.add(new Label("SETTINGS OVERLAY", style)).padBottom(30).row();
        settingsOverlayTable.add(new Label("( Coming Soon in Next Phase )", style)).padBottom(50).row();

        Label btnBack = new Label("[ BACK ]", style);
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                showMainMenu();
            }
        });
        settingsOverlayTable.add(btnBack);

        rootTable.addActor(settingsOverlayTable);
    }

    private void showMainMenu() {
        mainButtonsTable.setVisible(true);
        confirmTable.setVisible(false);
        settingsOverlayTable.setVisible(false);
    }

    private void showConfirm(String question, Runnable yesAction) {
        mainButtonsTable.setVisible(false);
        confirmTable.setVisible(true);
        settingsOverlayTable.setVisible(false);

        Label qLabel = (Label) confirmTable.getChildren().get(0);
        qLabel.setText(question);
        confirmTable.setUserObject(yesAction); // Simpan aksi ke memori table
    }

    private void showSettingsOverlay() {
        mainButtonsTable.setVisible(false);
        confirmTable.setVisible(false);
        settingsOverlayTable.setVisible(true);
    }

    // --- PUBLIC API UNTUK HUD MANAGER ---

    public void show() {
        rootTable.setVisible(true);
        rootTable.getColor().a = 0f;
        rootTable.clearActions();
        rootTable.addAction(Actions.fadeIn(0.2f, Interpolation.fade));
        showMainMenu();
    }

    public void hide() {
        rootTable.clearActions();
        rootTable.addAction(Actions.sequence(
            Actions.fadeOut(0.2f, Interpolation.fade),
            Actions.visible(false)
        ));
    }

    public Table getRootTable() {
        return rootTable;
    }

    public boolean isVisible() {
        return rootTable.isVisible();
    }

    public void dispose() {
        if (bgTexture != null) bgTexture.dispose();
    }
}
