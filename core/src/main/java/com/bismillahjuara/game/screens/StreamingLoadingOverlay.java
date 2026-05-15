package com.bismillahjuara.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bismillahjuara.game.pipeline.AsyncGameplayLoader;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Layar Loading Gameplay yang tidak akan pernah Freeze.
 * Berjalan beriringan dengan AsyncGameplayLoader.
 */
public class StreamingLoadingOverlay extends BaseScreen {

    private AsyncGameplayLoader loader;

    private Label loadingLabel;
    private Label progressLabel;
    private BitmapFont font;
    private StringBuilder sb;

    private float smoothProgress = 0f;
    private float dotTimer = 0f;
    private int dotCount = 0;

    public StreamingLoadingOverlay() {
        super();
        loader = new AsyncGameplayLoader();
        sb = new StringBuilder();
        setupPlaceholderUI();
    }

    private void setupPlaceholderUI() {
        // TODO: Replace dengan artwork background AAA & animated spinner
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().right().pad(50); // Taruh di pojok kanan bawah ala Genshin

        font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        loadingLabel = new Label("Memasuki Hutan...", style);
        progressLabel = new Label("0%", style);

        table.add(loadingLabel).padRight(20);
        table.add(progressLabel);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        super.render(delta); // Clear hitam murni

        // 1. UPDATE PIPELINE (Mencicil pembuatan World 1 langkah per frame)
        loader.update();

        // 2. FAKE SMOOTH PROGRESS BAR (Interpolasi)
        float target = loader.getProgress();
        smoothProgress = MathUtils.lerp(smoothProgress, target, delta * 3f);

        // 3. ANIMASI TITIK (Loading. / Loading.. / Loading...) tanpa alokasi objek
        dotTimer += delta;
        if (dotTimer > 0.4f) {
            dotTimer = 0f;
            dotCount = (dotCount + 1) % 4;
        }

        sb.setLength(0);
        sb.append("Memasuki Hutan");
        for (int i = 0; i < dotCount; i++) sb.append(".");
        loadingLabel.setText(sb);

        sb.setLength(0);
        sb.append((int)(smoothProgress * 100)).append("%");
        progressLabel.setText(sb);

        // 4. TRANSISI AMAN KE GAMEPLAY
        // Tunggu loader selesai DAN animasi bar benar-benar penuh (karena efek lerp)
        if (loader.isDone() && smoothProgress >= 0.99f) {
            GameScreen readyScreen = loader.getReadyGameScreen();
            // Masuk gameplay dengan fade in halus, tidak mengagetkan mata
            ScreenManager.getInstance().setScreen(readyScreen, new FadeTransition(1.5f));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        font.dispose();
    }
}
