package com.bismillahjuara.game.audio;

public enum AudioSFX {
    // --- UI ---
    UI_CLICK("sound/enemy/click.ogg"),
    UI_HOVER("sound/ui/hover.wav"),
    UI_CONFIRM("sound/ui/confirm.wav"),
    UI_BACK("sound/ui/back.wav"),

    // --- PLAYER FOOTSTEPS ---
    STEP_GRASS_1("sound/player/footstep_grass_01.ogg"),
    STEP_GRASS_2("sound/player/footstep_grass_02.ogg"),

    HEARTBEAT("sound/player/heartbeat.ogg"),

    // --- PLAYER ACTIONS ---
    JUMP("sound/player/jump.wav"),
    LAND("sound/player/land.wav"),
    JIMAT_THROW("sound/player/jimat_throw.wav"),

    // --- ENEMY ---
    ENEMY_ATTACK("sound/enemy/attack.wav"),
    ENEMY_DIE("sound/enemy/die.wav"),
    ENEMY_SCREAM("sound/enemy/scream.wav"),
    ENEMY_BURN("sound/enemy/burn.wav"),
    JUMPSCARE("sound/enemy/jumpscare.wav"),

    // --- ENVIRONMENT ---
    ENV_CHEST("sound/environment/heal.ogg"),
    SUKMA_01("sound/enemy/sukma_01.ogg"),
    SUKMA_02("sound/enemy/sukma_02.ogg"),

    // --- WEATHER (THUNDER) ---
    ENV_THUNDER_1("sound/environment/thunder.ogg"),
    ENV_THUNDER_2("sound/environment/thunder.ogg"),


    SEED_SHOOT("sound/player/jimat_throw.wav");
//    ENV_THUNDER_2("sound/environment/thunder_02.ogg"),
//    ENV_THUNDER_3("sound/environment/thunder_03.ogg");



    public final String path;
    AudioSFX(String path) { this.path = path; }
}
