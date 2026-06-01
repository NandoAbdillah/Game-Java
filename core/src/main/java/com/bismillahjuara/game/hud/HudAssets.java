package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class HudAssets {
    public final Skin skin;
    public final BitmapFont defaultFont;

    private Texture touchpadBgTex;
    private Texture touchpadKnobTex;
    private Texture buttonBgTex;
    private Texture buttonDownTex;

    // --- PAUSE MENU ASSETS ---
    public Texture pauseTitleTex;
    public Texture resumeTex;
    public Texture settingsTex;
    public Texture mainMenuTex;
    public Texture closeGameTex;
    public Texture pauseBtnMobileTex;

    public HudAssets() {
        skin = new Skin();
        defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.5f);
        skin.add("default", defaultFont);

        createTouchpadStyle();
        createButtonStyle();
        loadPauseAssets();
    }

    private void loadPauseAssets() {
        try {
            pauseTitleTex = loadTex("ui/pausemenu/PAUSE TITLE.png");
            resumeTex = loadTex("ui/pausemenu/RESUME.png");
            settingsTex = loadTex("ui/pausemenu/SETTINGS BUTTON.png");
            mainMenuTex = loadTex("ui/pausemenu/MAIN MENU.png");
            closeGameTex = loadTex("ui/pausemenu/CLOSE GAME.png");
            pauseBtnMobileTex = loadTex("ui/pausemenu/Pause Button.png");
        } catch (Exception e) {
            Gdx.app.error("HUD_ASSETS", "Gagal load gambar Pause Menu! Cek path/nama file.", e);
        }
    }

    private Texture loadTex(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }

    private void createTouchpadStyle() {

        Pixmap bgPixmap = new Pixmap(400, 400, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(1f, 1f, 1f, 0.15f);
        bgPixmap.fillCircle(200, 200, 200);
        touchpadBgTex = new Texture(bgPixmap);
        bgPixmap.dispose();
        Drawable touchBg = new TextureRegionDrawable(new TextureRegion(touchpadBgTex));


        Pixmap knobPixmap = new Pixmap(120, 120, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(1f, 1f, 1f, 0.7f);
        knobPixmap.fillCircle(60, 60, 60);
        touchpadKnobTex = new Texture(knobPixmap);
        knobPixmap.dispose();
        Drawable touchKnob = new TextureRegionDrawable(new TextureRegion(touchpadKnobTex));

        TouchpadStyle touchpadStyle = new TouchpadStyle();
        touchpadStyle.background = touchBg;
        touchpadStyle.knob = touchKnob;
        skin.add("default", touchpadStyle);
    }

    private void createButtonStyle() {

        Pixmap btnPixmap = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        btnPixmap.setColor(0.2f, 0.2f, 0.2f, 0.6f);
        btnPixmap.fillCircle(75, 75, 75);
        buttonBgTex = new Texture(btnPixmap);
        btnPixmap.dispose();
        Drawable btnUp = new TextureRegionDrawable(new TextureRegion(buttonBgTex));

        Pixmap btnDownPixmap = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        btnDownPixmap.setColor(0.8f, 0.8f, 0.8f, 0.8f);
        btnDownPixmap.fillCircle(75, 75, 75);
        buttonDownTex = new Texture(btnDownPixmap);
        btnDownPixmap.dispose();
        Drawable btnDown = new TextureRegionDrawable(new TextureRegion(buttonDownTex));

        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.up = btnUp;
        btnStyle.down = btnDown;
        btnStyle.font = defaultFont;
        btnStyle.fontColor = Color.WHITE;
        skin.add("default", btnStyle);
    }

    public void dispose() {
        skin.dispose();
        defaultFont.dispose();
        if (touchpadBgTex != null) touchpadBgTex.dispose();
        if (touchpadKnobTex != null) touchpadKnobTex.dispose();
        if (buttonBgTex != null) buttonBgTex.dispose();
        if (buttonDownTex != null) buttonDownTex.dispose();

        // Buang tekstur pause
        if (pauseTitleTex != null) pauseTitleTex.dispose();
        if (resumeTex != null) resumeTex.dispose();
        if (settingsTex != null) settingsTex.dispose();
        if (mainMenuTex != null) mainMenuTex.dispose();
        if (closeGameTex != null) closeGameTex.dispose();
        if (pauseBtnMobileTex != null) pauseBtnMobileTex.dispose();
    }
}
