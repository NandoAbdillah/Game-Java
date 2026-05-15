package com.bismillahjuara.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.bismillahjuara.game.input.GameInputHandler;

/**
 * High-level Settings Architect.
 * Membaca, menyimpan, dan MENGAPLIKASIKAN setting grafis/audio ke Engine.
 */
public class SettingsManager {

    private static SettingsManager instance;
    private SavePreferenceSystem saveSystem;

    // --- CACHED SETTINGS ---
    public boolean fullscreen;
    public boolean vsync;
    public int fpsLimit;
    public float masterVolume;
    public float bgmVolume;
    public float sfxVolume;
    public String graphicsPreset; // LOW, MEDIUM, HIGH, ULTRA

    private SettingsManager() {
        saveSystem = new SavePreferenceSystem();
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    public void loadSettings() {
        fullscreen = saveSystem.getBoolean("fullscreen", true);
        vsync = saveSystem.getBoolean("vsync", true);
        fpsLimit = saveSystem.getInteger("fpsLimit", 60);
        masterVolume = saveSystem.getFloat("masterVolume", 1.0f);
        bgmVolume = saveSystem.getFloat("bgmVolume", 0.8f);
        sfxVolume = saveSystem.getFloat("sfxVolume", 1.0f);
        graphicsPreset = saveSystem.getString("graphicsPreset", "HIGH");
    }

    public void saveAndApplySettings() {
        // 1. Simpan ke Disk
        saveSystem.saveBoolean("fullscreen", fullscreen);
        saveSystem.saveBoolean("vsync", vsync);
        saveSystem.saveInteger("fpsLimit", fpsLimit);
        saveSystem.saveFloat("masterVolume", masterVolume);
        saveSystem.saveFloat("bgmVolume", bgmVolume);
        saveSystem.saveFloat("sfxVolume", sfxVolume);
        saveSystem.saveString("graphicsPreset", graphicsPreset);
        saveSystem.flush();

        // 2. Aplikasikan ke Engine LibGDX
        applyToEngine();
    }

    public void applyToEngine() {
        // Grafis & Resolusi (Hanya berlaku penuh di PC, Android akan otomatis menyesuaikan)
        if (!GameInputHandler.IS_MOBILE) {
            if (fullscreen) {
                DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(currentMode);
            } else {
                Gdx.graphics.setWindowedMode(1280, 720); // Resolusi default window
            }
        }

        Gdx.graphics.setVSync(vsync);
        Gdx.graphics.setForegroundFPS(fpsLimit);

        // TODO: Aplikasikan Volume ke Audio Manager nanti
        // TODO: Aplikasikan Preset (LOW/MID/HIGH) ke Shader Config & Shadow Resolution
    }
}
