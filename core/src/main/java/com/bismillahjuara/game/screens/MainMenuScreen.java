package com.bismillahjuara.game.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Layar Menu Utama (Dummy Foundation).
 * Akan dipercantik pada Phase berikutnya.
 */
public class MainMenuScreen extends BaseScreen {

    private BitmapFont font;

    public MainMenuScreen() {
        super();
        setupMenuUI();
    }

    private void setupMenuUI() {
        Table table = new Table();
        table.setFillParent(true);

        font = new BitmapFont();
        font.getData().setScale(3f);

        // Judul Game
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.SCARLET);
        Label title = new Label("TIMUN\nThe Soil of Debt", titleStyle);
        title.setAlignment(com.badlogic.gdx.utils.Align.center);

        // Style Tombol Sederhana
        TextButtonStyle btnStyle = new TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.downFontColor = Color.GRAY; // Berubah abu-abu saat ditekan

        TextButton playButton = new TextButton("Start Nightmare", btnStyle);

        // Interaksi Tombol (TIDAK LAGI MASUK KE GAME SCREEN SECARA LANGSUNG)
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // AAA Flow: Main Menu -> Cutscene -> Loading -> Gameplay
                // Transisi fade pendek agar menu terasa responsif saat diklik
                ScreenManager.getInstance().setScreen(new StoryIntroScreen(), new FadeTransition(0.8f));
            }
        });

        // Susun ke Table
        table.add(title).padBottom(100).row();
        table.add(playButton);

        stage.addActor(table);
    }

    @Override
    public void dispose() {
        super.dispose();
        font.dispose();
    }
}
