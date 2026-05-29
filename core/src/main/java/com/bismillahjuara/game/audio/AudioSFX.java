package com.bismillahjuara.game.audio;

/**
 * Enum untuk Short Audio (RAM Loaded).
 * File berformat .wav/.ogg pendek, diload penuh ke RAM untuk latensi 0ms.
 */
public enum AudioSFX {
    // --- UI ---
    UI_CLICK("sound/ui/click.wav"),
    UI_HOVER("sound/ui/hover.wav"),
    UI_CONFIRM("sound/ui/confirm.wav"),
    UI_BACK("sound/ui/back.wav"),

    // --- PLAYER FOOTSTEPS ---
    STEP_GRASS_1("sound/player/footstep_grass_01.wav"),
    STEP_GRASS_2("sound/player/footstep_grass_02.wav"),
    STEP_GRASS_3("sound/player/footstep_grass_03.wav"),
    STEP_STONE_1("sound/player/footstep_stone_01.wav"),
    STEP_STONE_2("sound/player/footstep_stone_02.wav"),

    // --- PLAYER ACTIONS ---
    JUMP("sound/player/jump.wav"),
    LAND("sound/player/land.wav"),

    // --- ENEMY ---
    ENEMY_ATTACK("sound/enemy/attack.wav"),
    ENEMY_DIE("sound/enemy/die.wav"),
    ENEMY_SCREAM("sound/enemy/scream.wav"),

    // --- ENVIRONMENT ---
    ENV_WATERFALL("sound/environment/waterfall.wav"),
    ENV_CHEST("sound/environment/chest.wav"),
    ENV_DOOR("sound/environment/door.wav"),

    // --- WEATHER (THUNDER) ---
    ENV_THUNDER_1("sound/environment/thunder_01.ogg"),
    ENV_THUNDER_2("sound/environment/thunder_02.ogg"),
    ENV_THUNDER_3("sound/environment/thunder_03.ogg");

    public final String path;
    AudioSFX(String path) { this.path = path; }
}
