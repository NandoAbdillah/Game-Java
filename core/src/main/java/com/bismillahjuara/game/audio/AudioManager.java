package com.bismillahjuara.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.bismillahjuara.game.assets.GameAssets;
import com.bismillahjuara.game.settings.SettingsManager;

/**
 * AAA Centralized Audio Manager.
 * Menangani Crossfade, Routing Volume, dan Failsafe Asset.
 */
public class AudioManager {

    private static AudioManager instance;
    private SettingsManager settings;

    // --- STATE MUSIC ---
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

    // Timer untuk mengunci pemutaran audio
    private float footstepCooldownTimer = 0f;
    // Sesuaikan angka ini persis dengan durasi aslimu (6 detik)
    private final float FOOTSTEP_AUDIO_DURATION = 6.0f;
    // Volume khusus langkah kaki (0.15f - 0.25f) agar tidak merusak telinga
    public float footstepVolumeMultiplier = 0.20f;

    private AudioManager() {
        settings = SettingsManager.getInstance();
    }

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ==========================================
    // 1. MUSIC & AMBIENT SYSTEM
    // ==========================================

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

    // ==========================================
    // 2. SFX & UI SYSTEM
    // ==========================================

    public void playUI(AudioSFX sfx) {
        Sound sound = getSoundSafe(sfx.path);
        if (sound != null) sound.play(getUIVolume(), MathUtils.random(0.95f, 1.05f), 0);
    }

    public void playSFX(AudioSFX sfx) {
        Sound sound = getSoundSafe(sfx.path);
        if (sound != null) sound.play(getSFXVolume(), MathUtils.random(0.9f, 1.1f), 0);
    }

    // ==========================================
    // 3. SMART FOOTSTEP SYSTEM (6-SECOND LOCK)
    // ==========================================
    public void playRandomFootstep(boolean isGrass) {

        // 1. CEK COOLDOWN: Jika timer masih ada (audio masih muter), ABAIKAN TRIGGER!
        if (footstepCooldownTimer > 0) {
            // Opsional: Batasi log agar tidak spam setiap frame
            if (MathUtils.random() < 0.02f) {
                Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP BLOCKED (Audio 6 detik sedang Active/Cooldown)");
            }
            return;
        }

        // 2. RESET AUDIO LAMA (Failsafe)
        if (lastFootstepSound != null && lastFootstepId != -1) {
            lastFootstepSound.stop(lastFootstepId);
        }

        // 3. PUTAR AUDIO BARU (Kita paksakan ke STEP_GRASS_1 sesuai asetmu saat ini)
        AudioSFX chosen = AudioSFX.STEP_GRASS_1;

        Sound sound = getSoundSafe(chosen.path);
        if (sound != null) {
            lastFootstepSound = sound;

            // Terapkan Volume khusus (Kecilkan agar tidak pecah)
            float finalVolume = getSFXVolume() * footstepVolumeMultiplier;

            // Mainkan tanpa random pitch agar tempo 6 detiknya konstan
            lastFootstepId = sound.play(finalVolume, 1f, 0);

            // 4. AKTIFKAN COOLDOWN
            footstepCooldownTimer = FOOTSTEP_AUDIO_DURATION;

            Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP PLAY (Terkunci selama " + FOOTSTEP_AUDIO_DURATION + " detik. Volume: " + finalVolume + ")");
        }
    }

    /**
     * Mematikan paksa footstep (Opsional).
     * Bisa dipanggil dari Player.java jika karakter benar-benar diam/berhenti.
     */
    public void stopFootstep() {
        if (footstepCooldownTimer > 0 && lastFootstepSound != null && lastFootstepId != -1) {
            lastFootstepSound.stop(lastFootstepId);
            footstepCooldownTimer = 0f; // Reset kunci
            Gdx.app.log("AUDIO_FOOTSTEP", "FOOTSTEP STOPPED (Karakter Diam)");
        }
    }

    // ==========================================
    // 4. ENGINE UPDATE & ROUTING
    // ==========================================

    public void update(float delta) {
        // --- UPDATE FOOTSTEP COOLDOWN ---
        if (footstepCooldownTimer > 0) {
            footstepCooldownTimer -= delta;
        }

        // --- UPDATE CROSSFADE MUSIC ---
        // FIX: Hapus syarat nextMusic != null agar lagu bisa fade-out menuju keheningan (null)
        if (isMusicFading && currentMusic != null) {
            musicFadeTimer += delta;
            float progress = MathUtils.clamp(musicFadeTimer / musicFadeDuration, 0f, 1f);
            float targetVol = getMusicVolume();

            // Lagu sekarang perlahan menghilang
            currentMusic.setVolume(MathUtils.lerp(targetVol, 0f, progress));

            // Lagu selanjutnya perlahan muncul (jika ada)
            if (nextMusic != null) {
                nextMusic.setVolume(MathUtils.lerp(0f, targetVol, progress));
            }

            if (progress >= 1f) {
                currentMusic.stop();
                currentMusic = nextMusic; // Jika stopMusic, currentMusic akan menjadi null di sini (Benar)
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

    // ==========================================
    // 5. ASSET FAILSAFES
    // ==========================================

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
