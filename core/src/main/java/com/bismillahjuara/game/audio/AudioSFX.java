package com.bismillahjuara.game.audio;

/**
 * Enum untuk Audio RAM (.wav / .ogg pendek). Latensi 0ms.
 * Digunakan untuk UI, Player, dan Enemy.
 */
public enum AudioSFX {
    // --- UI ---
    UI_CLICK("sound/click.wav"),
    UI_HOVER("sound/hover.wav"),
    UI_BACK("sound/back.wav"),

    // --- PLAYER ---
    FOOTSTEP("sound/footstep.wav");
    // Tambahkan jump, land, attack nanti

    public final String path;
    AudioSFX(String path) { this.path = path; }
}
