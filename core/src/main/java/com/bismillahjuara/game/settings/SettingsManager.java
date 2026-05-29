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

    // --- ENUMS ---
    public enum GraphicsQuality { LOW, MEDIUM, HIGH, ULTRA }
    public enum FPSLimit {
        FPS_30(30), FPS_60(60), FPS_90(90), FPS_120(120), UNLIMITED(0);
        public final int value;
        FPSLimit(int value) { this.value = value; }
    }

    // --- CACHED SETTINGS ---
    public boolean fullscreen;
    public boolean vsync;
    public FPSLimit fpsLimit;
    public float masterVolume;
    public GraphicsQuality graphicsPreset;

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
        masterVolume = saveSystem.getFloat("masterVolume", 1.0f);

        try {
            fpsLimit = FPSLimit.valueOf(saveSystem.getString("fpsLimit", "FPS_60"));
            graphicsPreset = GraphicsQuality.valueOf(saveSystem.getString("graphicsPreset", "HIGH"));
        } catch (Exception e) {
            // Failsafe jika data harddisk corrupt
            fpsLimit = FPSLimit.FPS_60;
            graphicsPreset = GraphicsQuality.HIGH;
        }
    }

    public void saveAndApplySettings() {
        saveSystem.saveBoolean("fullscreen", fullscreen);
        saveSystem.saveBoolean("vsync", vsync);
        saveSystem.saveFloat("masterVolume", masterVolume);
        saveSystem.saveString("fpsLimit", fpsLimit.name());
        saveSystem.saveString("graphicsPreset", graphicsPreset.name());
        saveSystem.flush();

        applyToEngine();
    }

    public void applyToEngine() {
        // 1. RESOLUSI & FULLSCREEN (Hanya dieksekusi di PC)
        if (!GameInputHandler.IS_MOBILE) {
            if (fullscreen) {
                DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(currentMode);
            } else {
                Gdx.graphics.setWindowedMode(1280, 720);
            }
            Gdx.graphics.setVSync(vsync);
        }

        // 2. FPS LIMIT (Berlaku di PC & Mobile)
        Gdx.graphics.setForegroundFPS(fpsLimit.value);

        // 3. AUDIO (TODO: Integrasi dengan AudioManager saat dibuat nanti)
        // AudioManager.getInstance().setMasterVolume(masterVolume);

        // 4. GRAPHICS PRESET (Akan dibaca oleh SceneRenderer untuk Shader Config di fase selanjutnya)
        Gdx.app.log("SETTINGS", "Applied Preset: " + graphicsPreset.name() + " | FPS: " + fpsLimit.value);
    }
}
