package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bismillahjuara.game.transitions.FadeTransition;


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
            logoTexture = new Texture(Gdx.files.internal("STUDIO.png"));
            logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            studioLogo = new Image(logoTexture);
        } catch (Exception e) {
            Gdx.app.error("SPLASH", "File STUDIO.png tidak ditemukan di folder assets!", e);
            studioLogo = new Image();
        }

        studioLogo.getColor().a = 0f;

        rootTable.add(studioLogo)
            .width(400)
            .height(280)
            .expand()
            .center();

        stage.addActor(rootTable);
    }

    private void startCinematicSequence() {

        studioLogo.addAction(Actions.sequence(
            Actions.fadeIn(1.5f, com.badlogic.gdx.math.Interpolation.fade),
            Actions.delay(2.0f),
            Actions.fadeOut(1.0f, com.badlogic.gdx.math.Interpolation.fade),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    goToNextScreen();
                }
            })
        ));
    }

    private void goToNextScreen() {
        BootLoadingScreen nextScreen = new BootLoadingScreen();
        ScreenManager.getInstance().setScreen(nextScreen, new FadeTransition(1.0f));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (logoTexture != null) {
            logoTexture.dispose();
        }
    }
}
