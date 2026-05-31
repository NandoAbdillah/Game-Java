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

/**
 * Layar Loading Asynchronous Berstandar AAA.
 * Fitur: Smooth Interpolation, Background Image, Random Tips, Minimal GC.
 */
public class BootLoadingScreen extends BaseScreen {

    private Image barBackground;
    private Image barForeground;
    private Label progressLabel;
    private Label tipsLabel;

    private Texture bgTex;
    private Texture fgTex;

    // --- AAA Background System ---
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

        // Mulai antre aset ke AssetManager
        GameAssets.getInstance().queueBootAssets();
    }

    /**
     * Memasang gambar loading.jpeg dengan efek gelap agar teks terbaca (AAA Polish)
     */
    private void setupBackground() {
        // 1. Gambar Gunung
        try {
            bgImageTex = new Texture(Gdx.files.internal("ui/loading.jpeg"));
            bgImageTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image bgImage = new Image(bgImageTex);
            bgImage.setScaling(Scaling.fill); // Menjaga proporsi gambar menutupi seluruh layar
            bgImage.setFillParent(true);
            stage.addActor(bgImage);
        } catch (Exception e) {
            Gdx.app.error("LOADING_SCREEN", "Gambar loading.jpeg tidak ditemukan di assets!", e);
        }

        // 2. Dark Overlay (Hitam Transparan 60%)
        // Fungsinya sangat krusial: Memastikan teks putih selalu terbaca meskipun backgroundnya terang.
        Pixmap overlayPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        overlayPix.setColor(0f, 0f, 0f, 0.6f);
        overlayPix.fill();
        overlayTex = new Texture(overlayPix);
        overlayPix.dispose();

        Image darkOverlay = new Image(overlayTex);
        darkOverlay.setFillParent(true);
        stage.addActor(darkOverlay);
    }

    /**
     * Membuat UI langsung di RAM GPU (Pixmap) tanpa perlu load gambar dari disk.
     */
    private void createZeroDependencyUI() {
        // Buat tekstur background bar (Abu-abu gelap)
        Pixmap bgPix = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        bgPix.setColor(0.2f, 0.2f, 0.2f, 1f);
        bgPix.fill();
        bgTex = new Texture(bgPix);
        bgPix.dispose();

        // Buat tekstur foreground bar (Hijau cerah)
        Pixmap fgPix = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        fgPix.setColor(0.3f, 0.8f, 0.4f, 1f);
        fgPix.fill();
        fgTex = new Texture(fgPix);
        fgPix.dispose();

        // Gunakan FontManager agar konsisten dengan seluruh UI game
        BitmapFont font = FontManager.getInstance().getFont();

        barBackground = new Image(bgTex);
        barForeground = new Image(fgTex);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        progressLabel = new Label("0%", style);

        // Pilih Tips Acak
        String randomTip = AAA_TIPS[MathUtils.random(0, AAA_TIPS.length - 1)];
        Label.LabelStyle tipsStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        tipsLabel = new Label(randomTip, tipsStyle);
        tipsLabel.setAlignment(Align.center);

        // --- LAYOUTING ---
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(100f);

        table.add(tipsLabel).padBottom(30).row();

        // FIX AAA: Menggunakan WidgetGroup agar bar hijau dan abu-abu tertumpuk sejajar dengan presisi
        WidgetGroup barGroup = new WidgetGroup();

        barBackground.setSize(800, 30);
        barBackground.setPosition(0, 0);

        barForeground.setSize(0, 30); // Mulai dari width 0
        barForeground.setPosition(0, 0);

        barGroup.addActor(barBackground);
        barGroup.addActor(barForeground);

        table.add(barGroup).size(800, 30).row();
        table.add(progressLabel).padTop(20);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        super.render(delta); // Clear screen & render stage (UI)

        // 1. UPDATE ASSET MANAGER
        // Jika kembaliannya 'true', berarti loading file-file fisik sudah 100% selesai.
        boolean assetsLoaded = GameAssets.getInstance().manager.update();

        // FIX AAA: targetProgress ditambah lebih lambat (0.1f) agar durasi loading minimal 10 detik!
        targetProgress += delta * 0.1f;
        if (targetProgress > 1f) targetProgress = 1f;

        // Jika pakai AssetManager asli, aktifkan kode di bawah ini:
        // targetProgress = GameAssets.getInstance().manager.getProgress();

        // 2. FAKE SMOOTH INTERPOLATION
        // Bar akan mengejar target dengan kecepatan 5x (semakin jauh jaraknya, semakin cepat kejarannya)
        smoothProgress = MathUtils.lerp(smoothProgress, targetProgress, delta * 5f);

        // 3. UPDATE VISUAL UI
        barForeground.setWidth(800f * smoothProgress);

        // Update Text (No GC Allocations)
        sb.setLength(0);
        sb.append("MEMUAT... ").append((int)(smoothProgress * 100)).append("%");
        progressLabel.setText(sb);

        // 4. TRANSISI & SHADER WARMUP LATE-STAGE
        // Kita tunggu smoothProgress nyaris penuh agar animasi loading selesai dulu secara visual
        if (targetProgress >= 1f && smoothProgress >= 0.99f) {

            if (!isWarmupDone) {
                // Eksekusi compile shader PBR, ini mungkin akan membuat layar freeze 0.2 detik,
                // tapi karena bar loading sudah 100%, user tidak akan sadar/peduli!
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
