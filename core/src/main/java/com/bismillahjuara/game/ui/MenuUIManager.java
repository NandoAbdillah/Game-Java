package com.bismillahjuara.game.ui;

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

/**
 * Arsitektur Terpusat untuk Gaya UI (Skin).
 * Men-generate aset memori on-the-fly agar menu siap jalan tanpa perlu asset gambar .png eksternal.
 * Nanti kalau ada UI Artist, ganti Pixmap ini dengan memuat file .json Skin asli.
 */
public class MenuUIManager {
    private static MenuUIManager instance;
    public Skin skin;
    public BitmapFont titleFont;
    public BitmapFont bodyFont;

    private MenuUIManager() {
        skin = new Skin();

        // TODO: Replace dengan FreeTypeFontGenerator untuk Font Kustom (Zelda/RPG vibe)
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(1.5f);

        skin.add("title", new LabelStyle(titleFont, Color.WHITE));
        skin.add("default", new LabelStyle(bodyFont, Color.LIGHT_GRAY));

        generatePlaceholderStyles();
    }

    public static MenuUIManager getInstance() {
        if (instance == null) instance = new MenuUIManager();
        return instance;
    }

    private void generatePlaceholderStyles() {
        // --- BUTTON STYLE ---
        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.up = createDrawable(Color.valueOf("1a1a1aCC")); // Hitam transparan
        btnStyle.down = createDrawable(Color.valueOf("4d4d4dCC")); // Abu-abu
        btnStyle.over = createDrawable(Color.valueOf("333333CC")); // Hover abu gelap
        btnStyle.font = bodyFont;
        btnStyle.fontColor = Color.LIGHT_GRAY;
        btnStyle.overFontColor = Color.WHITE;
        skin.add("default", btnStyle);

        // --- SLIDER STYLE (Untuk Volume) ---
        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = createDrawable(Color.DARK_GRAY, 300, 10);
        sliderStyle.knob = createDrawable(Color.WHITE, 20, 30);
        skin.add("default-horizontal", sliderStyle);

        // --- CHECKBOX STYLE ---
        CheckBoxStyle checkStyle = new CheckBoxStyle();
        checkStyle.checkboxOff = createDrawable(Color.DARK_GRAY, 30, 30);
        checkStyle.checkboxOn = createDrawable(Color.GREEN, 30, 30);
        checkStyle.font = bodyFont;
        checkStyle.fontColor = Color.WHITE;
        skin.add("default", checkStyle);

        // --- WINDOW/PANEL STYLE ---
        WindowStyle winStyle = new WindowStyle();
        winStyle.background = createDrawable(Color.valueOf("000000E6")); // Hitam 90%
        winStyle.titleFont = bodyFont;
        skin.add("default", winStyle);
    }

    // Helper anti-GC untuk generate warna solid di UI
    private TextureRegionDrawable createDrawable(Color color) { return createDrawable(color, 1, 1); }
    private TextureRegionDrawable createDrawable(Color color, int w, int h) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
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
