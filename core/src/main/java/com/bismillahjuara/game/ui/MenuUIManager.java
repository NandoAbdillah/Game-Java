package com.bismillahjuara.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MenuUIManager {
    private static MenuUIManager instance;
    public Skin skin;
    public BitmapFont titleFont;
    public BitmapFont bodyFont;

    // --- SETTINGS ASSETS ---
    public TextureRegionDrawable bgSettings, headerAudio, headerGraphics;
    public TextureRegionDrawable lblMasterVol, lblFullscreen, lblVsync, lblFpsLimit;
    public TextureRegionDrawable btnApply, btnBack;

    private MenuUIManager() {
        skin = new Skin();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(1.5f);

        skin.add("title", new LabelStyle(titleFont, Color.WHITE));
        skin.add("default", new LabelStyle(bodyFont, Color.LIGHT_GRAY));

        loadSettingsAssets();
        generatePlaceholderStyles();
    }

    public static MenuUIManager getInstance() {
        if (instance == null) instance = new MenuUIManager();
        return instance;
    }

    private void loadSettingsAssets() {
        try {
            // Background & Headers
            bgSettings = loadDrawable("ui/settings/Background setting revisi.png");
            headerAudio = loadDrawable("ui/settings/AUDIO.png");
            headerGraphics = loadDrawable("ui/settings/GRAPHICS.png");

            // Labels
            lblMasterVol = loadDrawable("ui/settings/Master Volume.png");
            lblFullscreen = loadDrawable("ui/settings/Fullscreen.png");
            lblVsync = loadDrawable("ui/settings/V-sync.png");
            lblFpsLimit = loadDrawable("ui/settings/FPS Limit.png");

            // Buttons
            btnApply = loadDrawable("ui/settings/Apply.png");
            btnBack = loadDrawable("ui/settings/Back.png");

            // --- BUILD CUSTOM SLIDER STYLE ---
            SliderStyle sliderStyle = new SliderStyle();
            sliderStyle.background = loadDrawable("ui/settings/Parameter kosong.png");
            sliderStyle.knobBefore = loadDrawable("ui/settings/Parameter Filled.png");
            sliderStyle.knob = loadDrawable("ui/settings/Poin indicator.png");
            skin.add("custom-slider", sliderStyle);

            // --- BUILD CUSTOM CHECKBOX STYLE ---
            CheckBoxStyle checkStyle = new CheckBoxStyle();
            checkStyle.checkboxOff = loadDrawable("ui/settings/Tick kosong.png");
            checkStyle.checkboxOn = loadDrawable("ui/settings/Tick filled.png");
            checkStyle.font = bodyFont;
            checkStyle.fontColor = Color.WHITE;
            skin.add("custom-checkbox", checkStyle);

        } catch (Exception e) {
            Gdx.app.error("UI", "Gagal me-load aset PNG Settings! Pastikan nama file/huruf besar-kecil sesuai.", e);
        }
    }

    private TextureRegionDrawable loadDrawable(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void generatePlaceholderStyles() {
        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.up = createDrawable(Color.valueOf("1a1a1aCC"));
        btnStyle.down = createDrawable(Color.valueOf("4d4d4dCC"));
        btnStyle.font = bodyFont;
        skin.add("default", btnStyle);
    }

    private TextureRegionDrawable createDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    public void dispose() {
        if (skin != null) skin.dispose();
        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
    }
}
