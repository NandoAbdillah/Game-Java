package com.bismillahjuara.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Layar pertama yang dilihat pemain.
 * Fokus pada First Impression, rendering Logo, dan pengantar cinematic.
 */
public class SplashScreen extends BaseScreen {

    private Label studioLogoPlaceholder;
    private BitmapFont font; // TODO: Ganti dengan FreeTypeFontGenerator (Zelda-style) nanti

    public SplashScreen() {
        super();
        setupUI();
        startCinematicSequence();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // --- SYSTEM PLACEHOLDER ---
        // TODO: Ganti Label ini dengan Image(Texture) logo studio asli nanti
        font = new BitmapFont();
        font.getData().setScale(5f); // Kasih skala besar agar terlihat AAA

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        studioLogoPlaceholder = new Label("BismillahJuara\nStudio", style);
        studioLogoPlaceholder.setAlignment(Align.center);

        // Set alpha awal ke 0 (menghilang)
        studioLogoPlaceholder.getColor().a = 0f;

        rootTable.add(studioLogoPlaceholder).expand().center();
        stage.addActor(rootTable);
    }

    private void startCinematicSequence() {
        // Rahasia Animator UI AAA: Gunakan Sequence Action!
        // Alur: Pudar Masuk -> Tahan 2 detik -> Pudar Keluar -> Ganti Layar

        studioLogoPlaceholder.addAction(Actions.sequence(
            Actions.fadeIn(1.5f, com.badlogic.gdx.math.Interpolation.fade), // Fade in mulus 1.5 detik
            Actions.delay(2.0f),                                            // Tahan layar (Pemain membaca logo)
            Actions.fadeOut(1.0f, com.badlogic.gdx.math.Interpolation.fade), // Fade out 1 detik
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    goToNextScreen();
                }
            })
        ));
    }

    private void goToNextScreen() {
        // TODO: Nanti arahkan ke LoadingScreen atau MainMenuScreen
        // Untuk sekarang, kita arahkan langsung ke GameScreen dengan Transisi Fade (durasi 1 detik)

        GameScreen nextScreen = new GameScreen();
        ScreenManager.getInstance().setScreen(nextScreen, new FadeTransition(1.0f));
    }

    @Override
    public void dispose() {
        super.dispose();
        font.dispose(); // Ingat selalu buang memori aset lokal
    }
}
