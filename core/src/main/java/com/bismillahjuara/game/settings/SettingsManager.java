package com.bismillahjuara.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.bismillahjuara.game.input.GameInputHandler;

public class SettingsManager {

    private static SettingsManager instance;
    private SavePreferenceSystem saveSystem;

    public enum GraphicsQuality { LOW, MEDIUM, HIGH, ULTRA }
    public enum FPSLimit {
        FPS_30(30), FPS_60(60), FPS_90(90), FPS_120(120), UNLIMITED(0);
        public final int value;
        FPSLimit(int value) { this.value = value; }
    }

    public boolean fullscreen;
    public boolean vsync;
    public FPSLimit fpsLimit;
    public GraphicsQuality graphicsPreset;

    public float masterVolume;
    public float musicVolume;
    public float ambientVolume;
    public float sfxVolume;
    public float uiVolume;

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
        musicVolume = saveSystem.getFloat("musicVolume", 1.0f);
        ambientVolume = saveSystem.getFloat("ambientVolume", 1.0f);
        sfxVolume = saveSystem.getFloat("sfxVolume", 1.0f);
        uiVolume = saveSystem.getFloat("uiVolume", 1.0f);

        try {
            fpsLimit = FPSLimit.valueOf(saveSystem.getString("fpsLimit", "FPS_60"));
            graphicsPreset = GraphicsQuality.valueOf(saveSystem.getString("graphicsPreset", "HIGH"));
        } catch (Exception e) {
            fpsLimit = FPSLimit.FPS_60;
            graphicsPreset = GraphicsQuality.HIGH;
        }
    }

    public void saveAndApplySettings() {
        saveSystem.saveBoolean("fullscreen", fullscreen);
        saveSystem.saveBoolean("vsync", vsync);

        saveSystem.saveFloat("masterVolume", masterVolume);
        saveSystem.saveFloat("musicVolume", musicVolume);
        saveSystem.saveFloat("ambientVolume", ambientVolume);
        saveSystem.saveFloat("sfxVolume", sfxVolume);
        saveSystem.saveFloat("uiVolume", uiVolume);

        saveSystem.saveString("fpsLimit", fpsLimit.name());
        saveSystem.saveString("graphicsPreset", graphicsPreset.name());
        saveSystem.flush();

        applyToEngine();
    }

    public void applyToEngine() {
        if (!com.bismillahjuara.game.input.GameInputHandler.IS_MOBILE) {
            if (fullscreen) {
                DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(currentMode);
            } else {
                Gdx.graphics.setWindowedMode(1280, 720);
            }
            Gdx.graphics.setVSync(vsync);
        }

        Gdx.graphics.setForegroundFPS(fpsLimit.value);

        com.bismillahjuara.game.audio.AudioManager.getInstance().refreshRuntimeVolumes();
    }
}
