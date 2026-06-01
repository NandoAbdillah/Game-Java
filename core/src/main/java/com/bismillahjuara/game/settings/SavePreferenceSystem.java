package com.bismillahjuara.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SavePreferenceSystem {
    private static final String PREF_NAME = "BismillahJuara_Settings";
    private Preferences prefs;

    public SavePreferenceSystem() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
    }

    public void saveBoolean(String key, boolean val) { prefs.putBoolean(key, val); }
    public boolean getBoolean(String key, boolean def) { return prefs.getBoolean(key, def); }

    public void saveInteger(String key, int val) { prefs.putInteger(key, val); }
    public int getInteger(String key, int def) { return prefs.getInteger(key, def); }

    public void saveFloat(String key, float val) { prefs.putFloat(key, val); }
    public float getFloat(String key, float def) { return prefs.getFloat(key, def); }

    public void saveString(String key, String val) { prefs.putString(key, val); }
    public String getString(String key, String def) { return prefs.getString(key, def); }

    public void flush() {
        prefs.flush();
    }
}
