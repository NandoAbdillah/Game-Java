package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.FontManager;

public class StoryMenuScreen extends BaseScreen {

    // dbungging act
    public static int DEBUG_START_ACT = 1;

    private Texture bgImageTex;
    private Texture darkOverlayTex;

    private Table contentTable;
    private Label titleLabel;
    private Label descLabel;

    private TextButton btnPrev, btnNext, btnPlay, btnReturn;

    private int currentSlide = 0;

    // data slides
    private static class StorySlide {
        String title;
        String text;
        boolean canPlay;
        int actNumber;

        StorySlide(String title, String text, boolean canPlay, int actNumber) {
            this.title = title;
            this.text = text;
            this.canPlay = canPlay;
            this.actNumber = actNumber;
        }
    }

    private final StorySlide[] slides = {
        new StorySlide("PROLOG", "Hutan ini tidak pernah tidur...\nIbu bilang, monster itu mengincarku.\nTapi benda-benda pusaka ini...\nKenapa rasanya sangat berdosa saat menggunakannya?", false, 0),
        new StorySlide("ACT I : PUSAKA YANG HILANG", "Bukan sihir cahaya, melainkan benda-benda ritual peninggalan ibunya yang terasa berat dan mistis tersebar di dalam hutan.\n\nTimun harus menemukan seluruh pusaka tersebut jika ingin memiliki kekuatan untuk menghadapi Buto Ijo.", true, 1),
        new StorySlide("ACT II : TIMUN REVENGE'S", "Ibunya berpesan bahwa pusaka-pusaka tersebut menyimpan kekuatan yang cukup untuk membalas dendam.\n\nButo Ijo harus dihentikan sebelum semuanya terlambat. Lemparkan pusaka timun sebanyak 10 kali untuk mengalahkannya.", true, 2),
        new StorySlide("ENDING 1 : TRUTH", "Timun akhirnya mengetahui bahwa sosok yang selama ini dibencinya ternyata adalah ayahnya sendiri.\n\nButo Ijo tidak pernah berniat membunuhnya. Namun semuanya sudah terlambat...", false, 0),
        new StorySlide("ENDING 2 : THE DECEPTION", "Buto Ijo terbakar hingga lenyap. Barulah Timun mengetahui bahwa sosok tersebut adalah ayah kandungnya sendiri.\n\nDendam yang diwariskan ibunya ternyata berasal dari masa lalu yang jauh lebih kelam...", false, 0),
        new StorySlide("EPILOGUE", "Hutan kembali sunyi, namun kutukan itu belum berakhir...\n\n( TO BE CONTINUED )", false, 0)
    };

    public StoryMenuScreen() {
        super();
        setupBackground();
        setupUI();
        updateSlideData();
    }

    private void setupBackground() {
        try {
            FileHandle bgFile = Gdx.files.internal("ui/loading.jpeg");
            if (!bgFile.exists()) bgFile = Gdx.files.internal("ui/loading.png");

            if (bgFile.exists()) {
                bgImageTex = new Texture(bgFile);
                bgImageTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                Image bgImage = new Image(bgImageTex);
                bgImage.setScaling(Scaling.fill);
                bgImage.setFillParent(true);
                stage.addActor(bgImage);
            }
        } catch (Exception e) {
            Gdx.app.error("STORY_LOG", "Gambar background tidak ditemukan.");
        }

        Pixmap overlayPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        overlayPix.setColor(0f, 0f, 0f, 0.85f);
        overlayPix.fill();
        darkOverlayTex = new Texture(overlayPix);
        overlayPix.dispose();

        Image darkOverlay = new Image(darkOverlayTex);
        darkOverlay.setFillParent(true);
        stage.addActor(darkOverlay);
    }

    private void setupUI() {
        BitmapFont font = FontManager.getInstance().getFont();

        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.GOLD);
        Label.LabelStyle descStyle = new Label.LabelStyle(font, Color.WHITE);
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.LIGHT_GRAY;
        btnStyle.overFontColor = Color.WHITE;
        btnStyle.downFontColor = Color.DARK_GRAY;

        contentTable = new Table();
        contentTable.setTransform(true);
        contentTable.setOrigin(Align.center);

        titleLabel = new Label("", titleStyle);
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        descLabel = new Label("", descStyle);
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);

        contentTable.add(titleLabel).padBottom(40).row();
        contentTable.add(descLabel).width(1000).center();

        Table navTable = new Table();

        btnPrev = new TextButton("< PREV", btnStyle);
        btnNext = new TextButton("NEXT >", btnStyle);
        btnPlay = new TextButton("[ PLAY THIS ACT ]", btnStyle);
        btnReturn = new TextButton("RETURN TO MENU", btnStyle);

        btnPlay.addListener(new ClickListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                btnPlay.clearActions();
                btnPlay.addAction(Actions.color(Color.GREEN, 0.2f));
            }
            @Override public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                btnPlay.clearActions();
                btnPlay.addAction(Actions.color(Color.WHITE, 0.2f));
            }
            @Override public void clicked(InputEvent event, float x, float y) {
                playSelectedAct();
            }
        });

        btnPrev.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { changeSlide(-1); }});
        btnNext.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { changeSlide(1); }});
        btnReturn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                ScreenManager.getInstance().setScreen(new MainMenuScreen(), new FadeTransition(0.5f));
            }
        });

        // tombol navigasi
        navTable.add(btnPrev).width(200).padRight(20);
        navTable.add(btnPlay).width(300).padRight(20);
        navTable.add(btnNext).width(200).row();
        navTable.add(btnReturn).colspan(3).padTop(50).center();

        // layout utama
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.add(contentTable).expand().center().row();
        rootTable.add(navTable).bottom().padBottom(50);

        stage.addActor(rootTable);
    }

    private void changeSlide(int direction) {
        int nextSlide = currentSlide + direction;
        if (nextSlide < 0 || nextSlide >= slides.length) return;

        currentSlide = nextSlide;

        // burn transition
        contentTable.clearActions();
        contentTable.addAction(Actions.sequence(
            Actions.parallel(
                Actions.scaleTo(1.2f, 1.2f, 0.15f, Interpolation.fade),
                Actions.fadeOut(0.15f)
            ),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    updateSlideData();
                }
            }),
            Actions.parallel(
                Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.circleOut),
                Actions.fadeIn(0.2f)
            )
        ));
    }

    private void updateSlideData() {
        StorySlide slide = slides[currentSlide];
        titleLabel.setText(slide.title);
        descLabel.setText(slide.text);

        // visibilitas tombol
        btnPrev.setVisible(currentSlide > 0);
        btnNext.setVisible(currentSlide < slides.length - 1);
        btnPlay.setVisible(slide.canPlay);
    }

    private void playSelectedAct() {
        StorySlide slide = slides[currentSlide];
        if (slide.canPlay) {
            DEBUG_START_ACT = slide.actNumber;
            Gdx.app.log("STORY_LOG", "Starting game at ACT: " + DEBUG_START_ACT);

            ScreenManager.getInstance().setScreen(new StreamingLoadingOverlay(), new FadeTransition(1.0f));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bgImageTex != null) bgImageTex.dispose();
        if (darkOverlayTex != null) darkOverlayTex.dispose();
    }
}
