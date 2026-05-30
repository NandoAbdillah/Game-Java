package com.bismillahjuara.game.audio;

public enum AudioSFX {
    // --- UI ---
    UI_CLICK("sound/ui/click.wav"),
    UI_HOVER("sound/ui/hover.wav"),
    UI_CONFIRM("sound/ui/confirm.wav"),
    UI_BACK("sound/ui/back.wav"),

    // --- PLAYER FOOTSTEPS ---
    STEP_GRASS_1("sound/player/footstep_grass_01.ogg"),
    STEP_GRASS_2("sound/player/footstep_grass_02.ogg"),
//    STEP_GRASS_3("sound/player/footstep_grass_03.wav"),
    STEP_STONE_1("sound/player/footstep_stone_01.wav"),
    STEP_STONE_2("sound/player/footstep_stone_02.wav"),

    // --- PLAYER ACTIONS ---
    JUMP("sound/player/jump.wav"),
    LAND("sound/player/land.wav"),
    JIMAT_THROW("sound/player/jimat_throw.wav"), // BARU

    // --- ENEMY ---
    ENEMY_ATTACK("sound/enemy/attack.wav"),
    ENEMY_DIE("sound/enemy/die.wav"),
    ENEMY_SCREAM("sound/enemy/scream.wav"),
    ENEMY_BURN("sound/enemy/burn.wav"), // BARU
    JUMPSCARE("sound/enemy/jumpscare.wav"), // BARU

    // --- ENVIRONMENT ---
    ENV_WATERFALL("sound/environment/waterfall.wav"),
    ENV_CHEST("sound/environment/chest.wav"),
    ENV_DOOR("sound/environment/door.wav"),

    // --- WEATHER (THUNDER) ---
    ENV_THUNDER_1("sound/environment/thunder.ogg"),
    ENV_THUNDER_2("sound/environment/thunder.ogg");
//    ENV_THUNDER_2("sound/environment/thunder_02.ogg"),
//    ENV_THUNDER_3("sound/environment/thunder_03.ogg");

    public final String path;
    AudioSFX(String path) { this.path = path; }
}
