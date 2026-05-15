package com.bismillahjuara.game.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.settings.SettingsManager;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedButton;
import com.bismillahjuara.game.ui.MenuUIManager;

public class SettingsScreen extends BaseScreen {

    private Skin skin;
    private SettingsManager settings;

    // Komponen UI Interaktif
    private CheckBox fullscreenCheck;
    private CheckBox vsyncCheck;
    private Slider fpsSlider;
    private Slider masterVolSlider;

    public SettingsScreen() {
        super();
        this.skin = MenuUIManager.getInstance().skin;
        this.settings = SettingsManager.getInstance();
        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(100);

        // Kiri: Judul
        Label title = new Label("SYSTEM\nSETTINGS", skin, "title");
        title.setAlignment(Align.center);
        root.add(title).expandX().left().padRight(100);

        // Kanan: Panel Opsi
        Table options = new Table();

        // --- BUAT KOMPONEN ---
        fullscreenCheck = new CheckBox(" Fullscreen", skin);
        fullscreenCheck.setChecked(settings.fullscreen);

        vsyncCheck = new CheckBox(" VSync (Anti-Tearing)", skin);
        vsyncCheck.setChecked(settings.vsync);

        fpsSlider = new Slider(30, 144, 1, false, skin, "default-horizontal");
        fpsSlider.setValue(settings.fpsLimit);

        masterVolSlider = new Slider(0f, 1f, 0.05f, false, skin, "default-horizontal");
        masterVolSlider.setValue(settings.masterVolume);

        // --- SUSUN KOMPONEN ---
        options.add(new Label("Graphics", skin, "title")).padBottom(20).left().row();
        if (!GameInputHandler.IS_MOBILE) {
            options.add(fullscreenCheck).left().padBottom(15).row();
        }
        options.add(vsyncCheck).left().padBottom(15).row();

        options.add(new Label("FPS Limit / Frame Target", skin)).left().row();
        options.add(fpsSlider).width(400).padBottom(30).left().row();

        options.add(new Label("Audio", skin, "title")).padTop(20).padBottom(20).left().row();
        options.add(new Label("Master Volume", skin)).left().row();
        options.add(masterVolSlider).width(400).padBottom(40).left().row();

        // --- TOMBOL BAWAH ---
        Table btnTable = new Table();
        AnimatedButton applyBtn = new AnimatedButton("Apply & Save", skin, "default");
        AnimatedButton backBtn = new AnimatedButton("Back", skin, "default");

        applyBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { applySettings(); }
        });

        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { goBack(); }
        });

        btnTable.add(applyBtn).size(250, 60).padRight(20);
        btnTable.add(backBtn).size(200, 60);

        options.add(btnTable).left();

        root.add(options).expand().fill().right();
        stage.addActor(root);
    }

    private void applySettings() {
        settings.fullscreen = fullscreenCheck.isChecked();
        settings.vsync = vsyncCheck.isChecked();
        settings.fpsLimit = (int) fpsSlider.getValue();
        settings.masterVolume = masterVolSlider.getValue();

        settings.saveAndApplySettings();
        // TODO: UI Notification "Settings Applied!"
    }

    private void goBack() {
        ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
    }
}
