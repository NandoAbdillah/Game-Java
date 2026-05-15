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
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.assets.ShaderWarmup;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Layar Loading Asynchronous Berstandar AAA.
 * Fitur: Smooth Interpolation, Zero-Asset Dependency UI, Minimal GC.
 */
public class BootLoadingScreen extends BaseScreen {

    private Image barBackground;
    private Image barForeground;
    private Label progressLabel;
    private Label tipsLabel;
    private BitmapFont font;

    private Texture bgTex;
    private Texture fgTex;

    // Loading State
    private float targetProgress = 0f;
    private float smoothProgress = 0f;
    private boolean isWarmupDone = false;
    private StringBuilder sb; // Anti-GC Spike

    public BootLoadingScreen() {
        super();
        sb = new StringBuilder();

        createZeroDependencyUI();

        // Mulai antre aset ke AssetManager
        GameAssets.getInstance().queueBootAssets();
    }

    /**
     * Membuat UI langsung di RAM GPU (Pixmap) tanpa perlu load gambar dari disk.
     * Ini menjamin Loading Screen muncul seketika (0 ms delay).
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

        font = new BitmapFont();
        font.getData().setScale(2f);

        barBackground = new Image(bgTex);
        barForeground = new Image(fgTex);

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        progressLabel = new Label("0%", style);

        // TODO: Buat sistem Random Tips di masa depan
        Label.LabelStyle tipsStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        tipsLabel = new Label("Tips: Gunakan Biji Timun untuk mengikat kaki Buto Ijo...", tipsStyle);

        // --- LAYOUTING ---
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(100f);

        // Susun bar tumpang tindih pakai Stack atau cukup pakai absolute width nanti
        table.add(tipsLabel).padBottom(30).row();

        // Kita atur size bar background statis, bar foreground dinamis
        Table barTable = new Table();
        barTable.add(barBackground).size(800, 30);

        // Foreground di set absolute/mengambang di atas background
        barForeground.setSize(0, 30); // Mulai dari width 0
        barForeground.setPosition( (1920 - 800) / 2f, 100f ); // Hardcode position demi performa stack

        table.add(barTable).row();
        table.add(progressLabel).padTop(20);

        stage.addActor(table);
        stage.addActor(barForeground); // Tambahkan manual agar bisa dikontrol statis ukurannya
    }

    @Override
    public void render(float delta) {
        super.render(delta); // Clear screen & render stage (UI)

        // 1. UPDATE ASSET MANAGER
        // Jika kembaliannya 'true', berarti loading file-file fisik sudah 100% selesai.
        boolean assetsLoaded = GameAssets.getInstance().manager.update();

        // Karena sementara queueBootAssets kita kosong, kita buat dummy loading agar terlihat efeknya
        // TODO: Hapus dummy loading ini jika aset sungguhan sudah ditambahkan ke AssetManager
        targetProgress += delta * 0.5f;
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
        sb.append("Memuat... ").append((int)(smoothProgress * 100)).append("%");
        progressLabel.setText(sb);

        // 4. TRANSISI & SHADER WARMUP LATE-STAGE
        // Kita tunggu smoothProgress nyaris penuh agar animasi loading selesai dulu secara visual
        if (targetProgress >= 1f && smoothProgress >= 0.99f) {

            if (!isWarmupDone) {
                // Eksekusi compile shader PBR, ini mungkin akan membuat layar freeze 0.2 detik,
                // tapi karena bar loading sudah 100%, user tidak akan sadar/peduli! (Triks psikologi game)
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
        bgTex.dispose();
        fgTex.dispose();
        font.dispose();
    }
}
