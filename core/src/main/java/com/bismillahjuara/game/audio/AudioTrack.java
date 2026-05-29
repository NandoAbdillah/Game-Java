package com.bismillahjuara.game.audio;

/**
 * Enum untuk Audio Streaming (.ogg). Irit RAM.
 * Digunakan untuk Music dan Ambient yang panjang.
 */
public enum AudioTrack {
    // --- MUSIC ---
    THEME("sound/theme.ogg"),
    BATTLE_THEME("sound/battle_theme.ogg"),
    BOSS_THEME("sound/boss_theme.ogg"),
    CREDITS_THEME("sound/credits_theme.ogg"),
    VICTORY_THEME("sound/victory_theme.ogg"),

    // --- AMBIENT ---
    FOREST_AMBIENT("sound/ambient_forest.ogg"),
    HORROR_AMBIENT("sound/ambient_horror.ogg");
    // Tambahkan sesuai kebutuhan di masa depan

    public final String path;
    AudioTrack(String path) { this.path = path; }
}
