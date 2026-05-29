package com.bismillahjuara.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.audio.AudioTrack;

/**
 * Singleton Asset Manager dengan Defensive Loading.
 * Kebal dari Crash meskipun file fisik audio/gambar belum dibuat oleh artist.
 */
public class GameAssets {

    private static GameAssets instance;
    public AssetManager manager;

    private GameAssets() {
        manager = new AssetManager();
    }

    public static GameAssets getInstance() {
        if (instance == null) {
            instance = new GameAssets();
        }
        return instance;
    }

    // ==========================================================
    // HELPER: DEFENSIVE LOADING (ANTI-CRASH)
    // ==========================================================
    private void safeLoadMusic(String path) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, Music.class);
        } else {
            Gdx.app.log("ASSETS_WARNING", "Music diskip karena file belum ada: " + path);
        }
    }

    private void safeLoadSound(String path) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, Sound.class);
        } else {
            Gdx.app.log("ASSETS_WARNING", "SFX diskip karena file belum ada: " + path);
        }
    }
    // ==========================================================


    public void queueBootAssets() {
        // Load UI SFX secara aman (kalau file belum ada, diskip)
        safeLoadSound(AudioSFX.UI_CLICK.path);
        safeLoadSound(AudioSFX.UI_HOVER.path);
        safeLoadSound(AudioSFX.UI_BACK.path);

        // Load Theme Music awal secara aman
        safeLoadMusic(AudioTrack.THEME.path);
    }

    public void queueGameplayAssets() {
        // Load sisa audio di background secara aman
        for (AudioTrack track : AudioTrack.values()) {
            if (!manager.isLoaded(track.path)) {
                safeLoadMusic(track.path);
            }
        }

        for (AudioSFX sfx : AudioSFX.values()) {
            if (!manager.isLoaded(sfx.path)) {
                safeLoadSound(sfx.path);
            }
        }
    }

    public void dispose() {
        if (manager != null) manager.dispose();
    }
}
