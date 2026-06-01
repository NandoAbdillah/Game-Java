package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Layar pertama yang dilihat pemain.
 * Fokus pada First Impression, rendering Logo PNG, dan pengantar cinematic.
 */
public class SplashScreen extends BaseScreen {

    private Texture logoTexture;
    private Image studioLogo;

    public SplashScreen() {
        super();
        setupUI();
        startCinematicSequence();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        try {
            // Load file STUDIO.png dari folder assets
            logoTexture = new Texture(Gdx.files.internal("STUDIO.png"));
            // Set filter Linear agar logo tampil sangat tajam dan mulus (AAA Polish)
            logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            studioLogo = new Image(logoTexture);
        } catch (Exception e) {
            Gdx.app.error("SPLASH", "File STUDIO.png tidak ditemukan di folder assets!", e);
            studioLogo = new Image(); // Fallback kosong agar tidak crash
        }

        // Set alpha awal ke 0 (menghilang)
        studioLogo.getColor().a = 0f;

        rootTable.add(studioLogo)
            .width(400)
            .height(280)
            .expand()
            .center();

        stage.addActor(rootTable);
    }

    private void startCinematicSequence() {
        // Rahasia Animator UI AAA: Gunakan Sequence Action!
        // Alur: Pudar Masuk -> Tahan 2 detik -> Pudar Keluar -> Ganti Layar

        studioLogo.addAction(Actions.sequence(
            Actions.fadeIn(1.5f, com.badlogic.gdx.math.Interpolation.fade), // Fade in mulus 1.5 detik
            Actions.delay(2.0f),                                            // Tahan layar 2 detik
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
        // AAA Flow: SplashScreen -> BootLoadingScreen -> MainMenuScreen -> GameScreen
        BootLoadingScreen nextScreen = new BootLoadingScreen();
        ScreenManager.getInstance().setScreen(nextScreen, new FadeTransition(1.0f));
    }

    @Override
    public void dispose() {
        super.dispose();
        // Ingat selalu buang memori aset lokal agar tidak bocor (Memory Leak)
        if (logoTexture != null) {
            logoTexture.dispose();
        }
    }
}
