package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioTrack;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedImageButton;

public class MainMenuScreen extends BaseScreen {

    private static final String VIDEO_PATH = "video/menu.webm";
    private static final float VIDEO_MUTE_VOLUME = 0f;
    private static final float OVERLAY_ALPHA = 0.4f;

    private Table mainTable;

    private Texture titleTex;
    private Texture btnNewGameTex;
    private Texture btnContinueTex;
    private Texture btnStoryTex;
    private Texture btnSettingsTex;
    private Texture btnCreditsTex;
    private Texture btnExitTex;

    private VideoPlayer videoPlayer;
    private Texture darkOverlayTex;
    private Texture bgFallbackTex;

    private boolean videoInitialized = false;
    private boolean videoAvailable = false;

    private final Array<Texture> loadedTextures = new Array<>();

    public MainMenuScreen() {
        super();
        loadAssets();
        setupUI();
        beginThemeSound();
    }

    @Override
    public void show() {
        super.show();
        initVideoBackgroundIfNeeded();
        animateEntrance();

        if (videoPlayer != null) {
            videoPlayer.play();
        }
    }

    private void initVideoBackgroundIfNeeded() {
        if (videoInitialized) return;
        videoInitialized = true;

        FileHandle videoFile = Gdx.files.internal(VIDEO_PATH);
        Gdx.app.log("MAIN_MENU_VIDEO", "Init video: " + videoFile.path());

        if (!videoFile.exists()) {
            Gdx.app.error("MAIN_MENU_VIDEO", "File video tidak ditemukan: " + videoFile.path());
            videoAvailable = false;
            createOverlay();
            return;
        }

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();

            boolean loaded = videoPlayer.load(videoFile);
            if (!loaded) {
                Gdx.app.error("MAIN_MENU_VIDEO", "Video gagal di-load oleh decoder.");
                videoAvailable = false;
                createOverlay();
                return;
            }

            videoPlayer.setLooping(true);
            videoPlayer.setVolume(VIDEO_MUTE_VOLUME);
            videoPlayer.play();

            videoAvailable = true;
            Gdx.app.log("MAIN_MENU_VIDEO", "Video berhasil di-load dan diputar.");
        } catch (Exception e) {
            Gdx.app.error("MAIN_MENU_VIDEO", "Gagal inisialisasi video player.", e);
            videoAvailable = false;
        }

        createOverlay();
    }

    private void createOverlay() {
        if (darkOverlayTex != null) return;

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, OVERLAY_ALPHA);
        pix.fill();
        darkOverlayTex = new Texture(pix);
        pix.dispose();
    }

    private void loadAssets() {
        bgFallbackTex  = loadTexSafe("ui/mainmenu/BG_FALLBACK.png");

        titleTex       = loadTexSafe("ui/mainmenu/TITLE.png");
        btnNewGameTex  = loadTexSafe("ui/mainmenu/NEW_GAME.png");
        btnContinueTex = loadTexSafe("ui/mainmenu/CONTINUE.png");
        btnStoryTex    = loadTexSafe("ui/mainmenu/STORY_LOG.png");
        btnSettingsTex = loadTexSafe("ui/mainmenu/SETTINGS.png");
        btnCreditsTex  = loadTexSafe("ui/mainmenu/CREDIT.png");
        btnExitTex     = loadTexSafe("ui/mainmenu/EXIT.png");

        if (bgFallbackTex == null) {
            Pixmap pixmap = new Pixmap(1920, 1080, Pixmap.Format.RGB888);
            pixmap.setColor(new Color(0.15f, 0.05f, 0.05f, 1f));
            pixmap.fill();
            bgFallbackTex = new Texture(pixmap);
            loadedTextures.add(bgFallbackTex);
            pixmap.dispose();
        }
    }

    private Texture loadTexSafe(String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                Gdx.app.error("UI_ASSETS", "File tidak ditemukan: " + path);
                return null;
            }

            Texture tex = new Texture(file);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            loadedTextures.add(tex);
            return tex;
        } catch (Exception e) {
            Gdx.app.error("UI_ASSETS", "Gagal load texture: " + path, e);
            return null;
        }
    }

    private void setupUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.left().padLeft(150);

        if (titleTex != null) {
            Image titleImage = new Image(titleTex);
            mainTable.add(titleImage).size(600, 200).padBottom(80).left().row();
        }

        if (btnNewGameTex != null) {
            addMenuImageButton(btnNewGameTex, () -> startGame());
        }

        if (btnContinueTex != null) {
            addMenuImageButton(btnContinueTex, () -> {
                AudioManager.getInstance().stopMusic(1f);
                ScreenManager.getInstance().setScreen(new StreamingLoadingOverlay(), new FadeTransition(0.5f));

            });
        }

        if (btnStoryTex != null) {
            addMenuImageButton(btnStoryTex, () ->
                ScreenManager.getInstance().setScreen(new StoryMenuScreen(), new FadeTransition(0.5f))
            );
        }

        if (btnSettingsTex != null) {
            addMenuImageButton(btnSettingsTex, () ->
                ScreenManager.getInstance().setScreen(new SettingsScreen(), new FadeTransition(0.5f))
            );
        }

        if (btnCreditsTex != null) {
            addMenuImageButton(btnCreditsTex, () ->
                ScreenManager.getInstance().setScreen(new CreditsScreen(), new FadeTransition(0.5f))
            );
        }

        if (btnExitTex != null) {
            addMenuImageButton(btnExitTex, () -> Gdx.app.exit());
        }

        stage.addActor(mainTable);
    }

    private void addMenuImageButton(Texture texture, final Runnable action) {
        AnimatedImageButton btn = new AnimatedImageButton(texture);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });

        mainTable.add(btn).size(300, 80).padBottom(20).left().row();
    }

    private void beginThemeSound() {
        AudioManager.getInstance().playMusic(AudioTrack.THEME, 1.5f);
    }

    private void animateEntrance() {
        if (mainTable == null) return;

        float delay = 0f;
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : mainTable.getChildren()) {
            actor.addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.moveBy(-50f, 0f),
                Actions.delay(delay),
                Actions.parallel(
                    Actions.fadeIn(0.5f, Interpolation.fade),
                    Actions.moveBy(50f, 0f, 0.5f, Interpolation.circleOut)
                )
            ));
            delay += 0.15f;
        }
    }

    private void startGame() {
        ScreenManager.getInstance().setScreen(new StoryIntroScreen(), new FadeTransition(1.0f));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act(delta);

        SpriteBatch batch = (SpriteBatch) stage.getBatch();
        batch.begin();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        boolean drawn = false;

        if (videoAvailable && videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();

            if (frame != null) {
                drawCover(batch, frame, screenW, screenH);
                drawn = true;
            }
        }

        if (!drawn && bgFallbackTex != null) {
            drawCover(batch, bgFallbackTex, screenW, screenH);
        }

        if (darkOverlayTex != null) {
            batch.draw(darkOverlayTex, 0, 0, screenW, screenH);
        }

        batch.end();
        stage.draw();
    }

    private void drawCover(SpriteBatch batch, Texture tex, int screenW, int screenH) {
        float texW = tex.getWidth();
        float texH = tex.getHeight();

        float scale = Math.max((float) screenW / texW, (float) screenH / texH);
        float drawW = texW * scale;
        float drawH = texH * scale;
        float x = (screenW - drawW) * 0.5f;
        float y = (screenH - drawH) * 0.5f;

        batch.draw(tex, x, y, drawW, drawH);
    }

    @Override
    public void hide() {
        super.hide();
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (videoPlayer != null) {
            videoPlayer.dispose();
            videoPlayer = null;
        }

        if (darkOverlayTex != null) {
            darkOverlayTex.dispose();
            darkOverlayTex = null;
        }

        for (Texture tex : loadedTextures) {
            if (tex != null) tex.dispose();
        }
        loadedTextures.clear();
    }
}
