package com.bismillahjuara.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.settings.SettingsManager;


public class AudioManager {

    private static AudioManager instance;
    private SettingsManager settings;


    // STATE MUSIC
    private AudioTrack currentMusicTrack;
    private Music currentMusic;
    private Music nextMusic;
    private boolean isMusicFading = false;
    private float musicFadeTimer = 0f;
    private float musicFadeDuration = 2f;


    // --- STATE AMBIENT ---
    private AudioTrack currentAmbientTrack;
    private Music currentAmbient;

    // --- STATE FOOTSTEP (SMART COOLDOWN & VOLUME) ---
    private Sound lastFootstepSound;
    private long lastFootstepId = -1;



    private float footstepCooldownTimer = 0f;
    private final float FOOTSTEP_AUDIO_DURATION = 6.0f;
    public float footstepVolumeMultiplier = 0.20f;

    private AudioManager() {
        settings = SettingsManager.getInstance();
    }

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // MUSIC & AMBIENT SYSTEM
    public void playMusic(AudioTrack track, float fadeDuration) {
        if (currentMusicTrack == track) return; // Anti-Restart saat bolak-balik UI

        Music targetMusic = getMusicSafe(track.path);
        if (targetMusic == null) return;

        this.currentMusicTrack = track;

        if (fadeDuration > 0 && currentMusic != null) {
            this.nextMusic = targetMusic;
            this.musicFadeDuration = fadeDuration;
            this.musicFadeTimer = 0f;
            this.isMusicFading = true;

            this.nextMusic.setLooping(true);
            this.nextMusic.setVolume(0f);
            this.nextMusic.play();
        } else {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = targetMusic;
            currentMusic.setLooping(true);
            currentMusic.setVolume(getMusicVolume());
            currentMusic.play();
        }
    }

    public void stopMusic(float fadeOutDuration) {
        if (currentMusic == null) return;
        currentMusicTrack = null;

        if (fadeOutDuration > 0) {
            isMusicFading = true;
            nextMusic = null;
            musicFadeDuration = fadeOutDuration;
            musicFadeTimer = 0f;
        } else {
            currentMusic.stop();
            currentMusic = null;
            isMusicFading = false;
        }
    }

    public void playAmbient(AudioTrack track) {
        if (currentAmbientTrack == track) return;
        Music targetAmbient = getMusicSafe(track.path);
        if (targetAmbient == null) return;

        stopAmbient();
        currentAmbientTrack = track;
        currentAmbient = targetAmbient;
        currentAmbient.setLooping(true);
        currentAmbient.setVolume(getAmbientVolume());
        currentAmbient.play();
    }

    public void stopAmbient() {
        if (currentAmbient != null) currentAmbient.stop();
        currentAmbientTrack = null;
    }

    // SFX & UI SYSTEM
    public void playUI(AudioSFX sfx) {
        Sound sound = getSoundSafe(sfx.path);
        if (sound != null) sound.play(getUIVolume(), MathUtils.random(0.95f, 1.05f), 0);
    }

    public void playSFX(AudioSFX sfx) {
        Sound sound = getSoundSafe(sfx.path);
        if (sound != null) sound.play(getSFXVolume(), MathUtils.random(0.9f, 1.1f), 0);
    }

    // SMART FOOTSTEP SYSTEM
    public void playRandomFootstep(boolean isGrass) {

        // CEK COOLDOWN
        if (footstepCooldownTimer > 0) {

            if (MathUtils.random() < 0.02f) {
                Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP BLOCKED");
            }
            return;
        }

        // RESET AUDIO
        if (lastFootstepSound != null && lastFootstepId != -1) {
            lastFootstepSound.stop(lastFootstepId);
        }

        // PUTAR AUDIO BARU
        AudioSFX chosen = AudioSFX.STEP_GRASS_1;

        Sound sound = getSoundSafe(chosen.path);
        if (sound != null) {
            lastFootstepSound = sound;

            float finalVolume = getSFXVolume() * footstepVolumeMultiplier;

            lastFootstepId = sound.play(finalVolume, 1f, 0);
            footstepCooldownTimer = FOOTSTEP_AUDIO_DURATION;

            Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP PLAY (Terkunci selama " + FOOTSTEP_AUDIO_DURATION + " detik. Volume: " + finalVolume + ")");
        }
    }

    // Mematikan paksa footstep
    public void stopFootstep() {
        if (footstepCooldownTimer > 0 && lastFootstepSound != null && lastFootstepId != -1) {
            lastFootstepSound.stop(lastFootstepId);
            footstepCooldownTimer = 0f;
            Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP STOPPED (Karakter Diam)");
        }
    }

    // ENGINE UPDATE & ROUTING
    public void update(float delta) {
        if (footstepCooldownTimer > 0) {
            footstepCooldownTimer -= delta;
        }

        // UPDATE CROSSFADE MUSIC
        if (isMusicFading && currentMusic != null) {
            musicFadeTimer += delta;
            float progress = MathUtils.clamp(musicFadeTimer / musicFadeDuration, 0f, 1f);
            float targetVol = getMusicVolume();

            // Lagu sekarang perlahan menghilang
            currentMusic.setVolume(MathUtils.lerp(targetVol, 0f, progress));

            // Lagu selanjutnya perlahan muncul
            if (nextMusic != null) {
                nextMusic.setVolume(MathUtils.lerp(0f, targetVol, progress));
            }

            if (progress >= 1f) {
                currentMusic.stop();
                currentMusic = nextMusic;
                nextMusic = null;
                isMusicFading = false;
            }
        }
    }

    public void refreshRuntimeVolumes() {
        if (currentMusic != null && !isMusicFading) currentMusic.setVolume(getMusicVolume());
        if (currentAmbient != null) currentAmbient.setVolume(getAmbientVolume());
    }

    private float getMusicVolume()   { return settings.masterVolume * settings.musicVolume; }
    private float getAmbientVolume() { return settings.masterVolume * settings.ambientVolume; }
    private float getSFXVolume()     { return settings.masterVolume * settings.sfxVolume; }
    private float getUIVolume()      { return settings.masterVolume * settings.uiVolume; }

    // ASSET FAILSAFES
    private Music getMusicSafe(String path) {
        if (GameAssets.getInstance().manager.isLoaded(path, Music.class)) {
            return GameAssets.getInstance().manager.get(path, Music.class);
        }
        return null;
    }

    private Sound getSoundSafe(String path) {
        if (GameAssets.getInstance().manager.isLoaded(path, Sound.class)) {
            return GameAssets.getInstance().manager.get(path, Sound.class);
        }
        return null;
    }
}
