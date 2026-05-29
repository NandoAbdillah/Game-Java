package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioTrack;
import com.bismillahjuara.game.transitions.FadeTransition;
import com.bismillahjuara.game.ui.AnimatedImageButton;

public class MainMenuScreen extends BaseScreen {


    private Table mainTable;

    // PENYIMPANAN TEKSTUR
    private Texture titleTex;
    private Texture btnNewGameTex;
    private Texture btnContinueTex;
    private Texture btnStoryTex;     // KEMBALI HADIR!
    private Texture btnSettingsTex;
    private Texture btnCreditsTex;   // KEMBALI HADIR!
    private Texture btnExitTex;

    // List penampung agar gampang dibersihkan
    private Array<Texture> loadedTextures;

    public MainMenuScreen() {
        super();
        loadedTextures = new Array<>();
        loadAssets();
        setupUI();
        beginThemeSound();
        animateEntrance();

    }

    private void loadAssets() {
        // TODO: Sesuaikan nama file PNG-mu di sini!
        // Pastikan file-file ini ada di folder android/assets/ui/
        try {
            titleTex       = loadTex("ui/mainmenu/TITLE.png");
            btnNewGameTex  = loadTex("ui/mainmenu/NEW_GAME.png");
            btnContinueTex = loadTex("ui/mainmenu/CONTINUE.png");
            btnStoryTex    = loadTex("ui/mainmenu/STORY_LOG.png");
            btnSettingsTex = loadTex("ui/mainmenu/SETTINGS.png");
            btnCreditsTex  = loadTex("ui/mainmenu/CREDIT.png");
            btnExitTex     = loadTex("ui/mainmenu/EXIT.png");
        } catch (Exception e) {
            Gdx.app.error("UI_ASSETS", "Gagal load gambar PNG! Pastikan nama file benar.", e);
        }
    }

    // Helper method agar tekstur otomatis masuk ke daftar pembersihan
    private Texture loadTex(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        // Matikan filter blur agar gambar tetap tajam kalau di-scale
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        loadedTextures.add(tex);
        return tex;
    }

    private void setupUI() {
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.left().padLeft(150);

        // JUDUL GAME
        if (titleTex != null) {
            Image titleImage = new Image(titleTex);
            mainTable.add(titleImage).size(600, 200).padBottom(80).left().row();
        }

        // TOMBOL-TOMBOL (MURNI GAMBAR PNG)

        if (btnNewGameTex != null) {
            addMenuImageButton(btnNewGameTex, new Runnable() {
                @Override public void run() { startGame(); }
            });
        }

        if (btnContinueTex != null) {
            addMenuImageButton(btnContinueTex, new Runnable() {
                @Override public void run() { /* TODO: Load Save Game */ }
            });
        }

        if (btnStoryTex != null) {
            addMenuImageButton(btnStoryTex, new Runnable() {
                @Override public void run() { ScreenManager.getInstance().setScreen(new StoryMenuScreen(), new FadeTransition(0.5f)); }
            });
        }

        if (btnSettingsTex != null) {
            addMenuImageButton(btnSettingsTex, new Runnable() {
                @Override public void run() { ScreenManager.getInstance().setScreen(new SettingsScreen(), new FadeTransition(0.5f)); }
            });
        }

        if (btnCreditsTex != null) {
            addMenuImageButton(btnCreditsTex, new Runnable() {
                @Override public void run() { ScreenManager.getInstance().setScreen(new CreditsScreen(), new FadeTransition(0.5f)); }
            });
        }

        if (btnExitTex != null) {
            addMenuImageButton(btnExitTex, new Runnable() {
                @Override public void run() { Gdx.app.exit(); }
            });
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

        // Setup ukuran dasar tombol png
        mainTable.add(btn).size(300, 80).padBottom(20).left().row();
    }


    private  void beginThemeSound()  {
        AudioManager.getInstance().playMusic(AudioTrack.THEME, 1.5f);
    }
    private void animateEntrance() {
        // Efek ui entrance
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
    public void dispose() {
        super.dispose();

        // PENTING SEKALI: Bersihkan semua RAM dari gambar PNG saat menu ini ditutup!
        // Inilah yang mencegah game kamu Crash atau Not Responding.
        for (Texture tex : loadedTextures) {
            if (tex != null) tex.dispose();
        }
        loadedTextures.clear();
    }
}
