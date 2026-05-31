package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.settings.SettingsManager;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.FontManager;

/**
 * AAA Cinematic Prologue Screen.
 * 100% Video & Audio, no lore text. Fades elegantly.
 */
public class StoryIntroScreen extends BaseScreen {

    private static final String VIDEO_PATH = "video/prolog.webm";
    private static final String AUDIO_PATH = "sound/prolog_voice.ogg";

    // Overlay sangat tipis (0.15) agar video tetap mendominasi visual
    private static final float OVERLAY_ALPHA = 0.10f;

    // --- VIDEO & AUDIO SYSTEM ---
    private VideoPlayer videoPlayer;
    private Music storyVoiceOver;
    private Texture darkOverlayTex;
    private Texture solidBlackTex;

    private Image cinematicFader; // Layar hitam untuk efek fade in & fade out

    private boolean videoInitialized = false;
    private boolean videoAvailable = false;
    private boolean isSkipping = false;

    // --- AUDIO FADE SYSTEM ---
    private boolean isAudioFading = false;
    private float audioFadeTimer = 0.5f; // Durasi fade out audio
    private float initialAudioVolume = 0f;

    public StoryIntroScreen() {
        super();
        AudioManager.getInstance().stopMusic(1.5f);

        createTextures();
        prepareVoiceOverAudio();
        setupCinematicFader(); // Layar masih gelap gulita saat init
        setupSkipButton();     // Tombol skip disiapkan tersembunyi
    }

    private void prepareVoiceOverAudio() {
        try {
            FileHandle audioFile = Gdx.files.internal(AUDIO_PATH);
            if (audioFile.exists()) {
                storyVoiceOver = Gdx.audio.newMusic(audioFile);
                storyVoiceOver.setLooping(false);

                float masterVol = SettingsManager.getInstance().masterVolume;
                float sfxVol = SettingsManager.getInstance().sfxVolume;
                storyVoiceOver.setVolume(masterVol * sfxVol);
            } else {
                Gdx.app.error("STORY_AUDIO", "File suara cerita tidak ditemukan di: " + AUDIO_PATH);
            }
        } catch (Exception e) {
            Gdx.app.error("STORY_AUDIO", "Gagal me-load file suara cerita.", e);
        }
    }

    private void createTextures() {
        Pixmap pixOver = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixOver.setColor(0f, 0f, 0f, OVERLAY_ALPHA);
        pixOver.fill();
        darkOverlayTex = new Texture(pixOver);
        pixOver.dispose();

        Pixmap pixBlack = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixBlack.setColor(Color.BLACK);
        pixBlack.fill();
        solidBlackTex = new Texture(pixBlack);
        pixBlack.dispose();
    }

    private void setupCinematicFader() {
        cinematicFader = new Image(solidBlackTex);
        cinematicFader.setFillParent(true);
        cinematicFader.setTouchable(Touchable.disabled); // Agar tidak menghalangi klik
        stage.addActor(cinematicFader); // Ditaruh paling awal, nanti kita tarik ke paling depan (Z-Index)
    }

    private void setupSkipButton() {
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = FontManager.getInstance().getFont();
        btnStyle.fontColor = new Color(0.8f, 0.8f, 0.8f, 1f); // Putih redup
        btnStyle.overFontColor = Color.WHITE; // Terang saat di-hover

        final TextButton skipBtn = new TextButton("SKIP", btnStyle);
        skipBtn.setTransform(true); // Wajib true agar scale animasi berfungsi
        skipBtn.setOrigin(Align.center);

        // Posisi awal agak ke bawah agar efek slide-up terlihat
        skipBtn.setPosition(1750, 20);
        skipBtn.getColor().a = 0f;
        skipBtn.setTouchable(Touchable.disabled);

        // Muncul setelah 30 DETIK: Fade In + Slide Up
        skipBtn.addAction(Actions.sequence(
            Actions.delay(30f),
            Actions.parallel(
                Actions.fadeIn(2f, Interpolation.fade),
                Actions.moveBy(0, 30f, 2f, Interpolation.circleOut)
            ),
            Actions.run(() -> skipBtn.setTouchable(Touchable.enabled))
        ));

        // Animasi Hover ala AAA
        skipBtn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (skipBtn.getTouchable() == Touchable.enabled) {
                    skipBtn.addAction(Actions.scaleTo(1.15f, 1.15f, 0.2f, Interpolation.smooth));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (skipBtn.getTouchable() == Touchable.enabled) {
                    skipBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.smooth));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (skipBtn.getTouchable() == Touchable.enabled) {
                    proceedToLoading();
                }
            }
        });

        stage.addActor(skipBtn);
    }

    @Override
    public void show() {
        super.show();
        // Pastikan fader (layar hitam) ada di tumpukan paling atas layar
        cinematicFader.toFront();

        initVideoBackgroundIfNeeded();

        // 1.5 Detik "Fade from Black" perlahan membuka pemandangan video
        cinematicFader.addAction(Actions.sequence(
            Actions.fadeOut(1.5f),
            Actions.visible(false)
        ));
    }

    private void initVideoBackgroundIfNeeded() {
        if (videoInitialized) return;
        videoInitialized = true;

        FileHandle videoFile = Gdx.files.internal(VIDEO_PATH);

        if (!videoFile.exists()) {
            Gdx.app.error("STORY_VIDEO", "File video tidak ditemukan.");
            videoAvailable = false;
            triggerFailsafeLoading();
            return;
        }

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();

            if (!videoPlayer.load(videoFile)) {
                videoAvailable = false;
                triggerFailsafeLoading();
                return;
            }

            // SETTING AAA: Jangan looping, volume 0 (karena suara dipisah).
            videoPlayer.setLooping(false);
            videoPlayer.setVolume(0f);

            // LISTENER PENYELESAIAN VIDEO (Saat 57 detik selesai)
            videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
                @Override
                public void onCompletionListener(FileHandle file) {
                    // Karena listener berjalan di thread terpisah, kita wajib oper ke Main Thread
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            proceedToLoading();
                        }
                    });
                }
            });

            videoPlayer.play();

            if (storyVoiceOver != null) {
                storyVoiceOver.play();
            }

            videoAvailable = true;

        } catch (Throwable t) {
            Gdx.app.error("STORY_VIDEO", "CRASH SAAT LOAD VIDEO!", t);
            videoAvailable = false;
            triggerFailsafeLoading();
        }
    }

    private void triggerFailsafeLoading() {
        // Jika file WEBM rusak/hilang, tunggu 3 detik lalu skip ke loading
        stage.addAction(Actions.sequence(
            Actions.delay(3f),
            Actions.run(new Runnable() {
                @Override public void run() { proceedToLoading(); }
            })
        ));
    }

    private void proceedToLoading() {
        if (isSkipping) return;
        isSkipping = true;

        // 1. Mulai turunkan volume suara perlahan (Fade Out)
        isAudioFading = true;
        if (storyVoiceOver != null) {
            initialAudioVolume = storyVoiceOver.getVolume();
        }

        // 2. Mainkan Layar Hitam "Fade To Black" selama 1 Detik
        cinematicFader.setVisible(true);
        cinematicFader.toFront();
        cinematicFader.addAction(Actions.sequence(
            Actions.fadeIn(1.0f), // Gelap dalam 1 detik
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    // 3. Masuk ke loading screen tanpa transisi engine (karena layarnya sudah hitam legam)
                    ScreenManager.getInstance().setScreen(new StreamingLoadingOverlay(), null);
                }
            })
        ));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // --- MANAJEMEN AUDIO FADE OUT (0.5 Detik) ---
        if (isAudioFading && storyVoiceOver != null && storyVoiceOver.isPlaying()) {
            audioFadeTimer -= delta;
            if (audioFadeTimer <= 0) {
                storyVoiceOver.stop();
            } else {
                // Lerp volume dari nilai awal menuju 0
                float newVol = initialAudioVolume * (audioFadeTimer / 0.5f);
                storyVoiceOver.setVolume(newVol);
            }
        }

        stage.act(delta);

        SpriteBatch batch = (SpriteBatch) stage.getBatch();
        batch.begin();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        boolean drawn = false;

        // 1. RENDER VIDEO PROLOG
        if (videoAvailable && videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();

            if (frame != null) {
                drawCover(batch, frame, screenW, screenH);
                drawn = true;
            }
        }

        // 2. FALLBACK JIKA VIDEO GAGAL
        if (!drawn && solidBlackTex != null) {
            batch.draw(solidBlackTex, 0, 0, screenW, screenH);
        }

        // 3. OVERLAY TIPIS (0.15 Alpha)
        if (darkOverlayTex != null) {
            batch.draw(darkOverlayTex, 0, 0, screenW, screenH);
        }

        batch.end();

        // 4. RENDER UI STAGE (Skip Button & Cinematic Fader)
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
        if (videoPlayer != null) videoPlayer.pause();
        if (storyVoiceOver != null && storyVoiceOver.isPlaying()) storyVoiceOver.pause();
    }

    @Override
    public void dispose() {
        super.dispose();
        // Jangan dispose customFont di sini karena FontManager yang mengurusnya!

        if (videoPlayer != null) {
            videoPlayer.dispose();
            videoPlayer = null;
        }
        if (storyVoiceOver != null) {
            storyVoiceOver.dispose();
            storyVoiceOver = null;
        }
        if (darkOverlayTex != null) darkOverlayTex.dispose();
        if (solidBlackTex != null) solidBlackTex.dispose();
    }
}
