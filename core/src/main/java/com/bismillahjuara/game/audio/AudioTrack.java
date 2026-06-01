package com.bismillahjuara.game.audio;

public enum AudioTrack {
    // MUSIC
    THEME("sound/theme.ogg"),
    BATTLE_THEME("sound/battle_theme.ogg"),
    BOSS_THEME("sound/environment/hutan.ogg"),
    CREDITS_THEME("sound/credits.ogg"),
    VICTORY_THEME("sound/victory_theme.ogg"),

    // AMBIENT
    FOREST_AMBIENT("sound/environment/hutan.ogg"),

    HORROR_AMBIENT("sound/ambient_horror.ogg");


    public final String path;
    AudioTrack(String path) { this.path = path; }
}
