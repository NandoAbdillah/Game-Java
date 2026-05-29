package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.transitions.FadeTransition;

/**
 * Fake Cinematic Screen.
 * Memberikan Lore/Cerita sebelum masuk ke layar streaming loading.
 */
public class StoryIntroScreen extends BaseScreen {

    private BitmapFont font;
    private Label storyText;
    private boolean isSkipping = false;

    public StoryIntroScreen() {
        super();
        setupCinematic();
        setupSkipButton();

        AudioManager.getInstance().stopMusic(1.5f);
    }

    private void setupCinematic() {
        // TODO: Replace dengan Scene2D Image(Texture) sequence / VideoPlayer jika engine support
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        String lore = "Hutan ini tidak pernah tidur...\n\n" +
            "Ibu bilang, monster itu mengincarku.\n" +
            "Tapi benda-benda pusaka ini...\n" +
            "Kenapa rasanya sangat berdosa saat menggunakannya?";

        storyText = new Label(lore, style);
        storyText.setAlignment(Align.center);
        storyText.getColor().a = 0f; // Sembunyikan awal

        Table table = new Table();
        table.setFillParent(true);
        table.add(storyText).expand().center();
        stage.addActor(table);

        // Sequence Cerita Ala AAA
        storyText.addAction(Actions.sequence(
            Actions.fadeIn(2f),
            Actions.delay(4f), // Biarkan pemain membaca 4 detik
            Actions.fadeOut(2f),
            Actions.run(new Runnable() {
                @Override public void run() { proceedToLoading(); }
            })
        ));
    }

    private void setupSkipButton() {
        // TODO: Upgrade ke "Hold to Skip" (Tahan untuk skip) dengan animasi progress melingkar
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.LIGHT_GRAY;
        btnStyle.downFontColor = Color.WHITE;

        TextButton skipBtn = new TextButton("Skip >>", btnStyle);
        skipBtn.setPosition(1700, 50); // Pojok kanan bawah di 1920x1080 Viewport
        skipBtn.getColor().a = 0f;

        // Munculkan tombol skip sedikit telat agar cinematic terasa organik
        skipBtn.addAction(Actions.sequence(Actions.delay(1.5f), Actions.fadeIn(1f)));

        skipBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                proceedToLoading();
            }
        });

        stage.addActor(skipBtn);
    }

    private void proceedToLoading() {
        if (isSkipping) return; // Cegah double klik
        isSkipping = true;

        // Batal semua animasi story yang sedang berjalan
        storyText.clearActions();

        // Maju ke Loading Streaming
        ScreenManager.getInstance().setScreen(new StreamingLoadingOverlay(), new FadeTransition(0.5f));
    }

    @Override
    public void dispose() {
        super.dispose();
        font.dispose();
    }
}
