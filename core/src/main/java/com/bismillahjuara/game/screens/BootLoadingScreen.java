package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.assets.ShaderWarmup;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.FontManager;

public class BootLoadingScreen extends BaseScreen {

    private Image barBackground;
    private Image barForeground;
    private Label progressLabel;
    private Label tipsLabel;

    private Texture bgTex;
    private Texture fgTex;

    private Texture bgImageTex;
    private Texture overlayTex;

    // Loading State
    private float targetProgress = 0f;
    private float smoothProgress = 0f;
    private boolean isWarmupDone = false;
    private StringBuilder sb; // Anti-GC Spike

    // --- RANDOM TIPS SYSTEM ---
    private static final String[] AAA_TIPS = {
        "TIPS: Gunakan Biji Timun untuk menyerang Sukma Gowong dari jarak yang aman.",
        "TIPS: Jangan terlalu lama berdiam diri. Hutan ini selalu mengawasimu...",
        "TIPS: Temukan 3 Relik Pusaka peninggalan ibumu untuk memancing keluarnya Buto Ijo.",
        "TIPS: Gunakan Earphone atau Headphone untuk pengalaman bermain yang lebih mencekam.",
        "TIPS: Berlari (Sprint) membantumu cepat kabur, tapi suaranya bisa memancing bahaya.",
        "TIPS: Buto Ijo sangat kuat. Ia membutuhkan 10 hantaman pusaka sebelum akhirnya tumbang.",
        "TIPS: Perhatikan sekeliling. Benda pusaka memancarkan cahaya emas di dalam kegelapan.",
        "TIPS: Sukma Gowong yang terbakar akan hancur menjadi debu. Tetap waspada!"
    };

    public BootLoadingScreen() {
        super();
        sb = new StringBuilder();

        setupBackground();
        createZeroDependencyUI();

        GameAssets.getInstance().queueBootAssets();
    }


    private void setupBackground() {
        try {
            bgImageTex = new Texture(Gdx.files.internal("ui/loading.jpeg"));
            bgImageTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image bgImage = new Image(bgImageTex);
            bgImage.setScaling(Scaling.fill);
            bgImage.setFillParent(true);
            stage.addActor(bgImage);
        } catch (Exception e) {
            Gdx.app.error("LOADING_SCREEN", "Gambar loading.jpeg tidak ditemukan di assets!", e);
        }

        Pixmap overlayPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        overlayPix.setColor(0f, 0f, 0f, 0.6f);
        overlayPix.fill();
        overlayTex = new Texture(overlayPix);
        overlayPix.dispose();

        Image darkOverlay = new Image(overlayTex);
        darkOverlay.setFillParent(true);
        stage.addActor(darkOverlay);
    }

    private void createZeroDependencyUI() {
        Pixmap bgPix = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        bgPix.setColor(0.2f, 0.2f, 0.2f, 1f);
        bgPix.fill();
        bgTex = new Texture(bgPix);
        bgPix.dispose();

        Pixmap fgPix = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        fgPix.setColor(0.3f, 0.8f, 0.4f, 1f);
        fgPix.fill();
        fgTex = new Texture(fgPix);
        fgPix.dispose();

        BitmapFont font = FontManager.getInstance().getFont();

        barBackground = new Image(bgTex);
        barForeground = new Image(fgTex);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        progressLabel = new Label("0%", style);

        String randomTip = AAA_TIPS[MathUtils.random(0, AAA_TIPS.length - 1)];
        Label.LabelStyle tipsStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        tipsLabel = new Label(randomTip, tipsStyle);
        tipsLabel.setAlignment(Align.center);

        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(100f);

        table.add(tipsLabel).padBottom(30).row();

        WidgetGroup barGroup = new WidgetGroup();

        barBackground.setSize(800, 30);
        barBackground.setPosition(0, 0);

        barForeground.setSize(0, 30);
        barForeground.setPosition(0, 0);

        barGroup.addActor(barBackground);
        barGroup.addActor(barForeground);

        table.add(barGroup).size(800, 30).row();
        table.add(progressLabel).padTop(20);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        boolean assetsLoaded = GameAssets.getInstance().manager.update();

        targetProgress += delta * 0.1f;
        if (targetProgress > 1f) targetProgress = 1f;


        smoothProgress = MathUtils.lerp(smoothProgress, targetProgress, delta * 5f);

        barForeground.setWidth(800f * smoothProgress);

        sb.setLength(0);
        sb.append("MEMUAT... ").append((int)(smoothProgress * 100)).append("%");
        progressLabel.setText(sb);

        if (targetProgress >= 1f && smoothProgress >= 0.99f) {

            if (!isWarmupDone) {

                ShaderWarmup.executeWarmup();
                isWarmupDone = true;
            } else {
                // Pindah ke Main Menu
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(1f));
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bgTex != null) bgTex.dispose();
        if (fgTex != null) fgTex.dispose();
        if (bgImageTex != null) bgImageTex.dispose();
        if (overlayTex != null) overlayTex.dispose();
    }
}
