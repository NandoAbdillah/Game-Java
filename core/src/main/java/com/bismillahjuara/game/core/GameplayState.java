package com.bismillahjuara.game.core;

/**
 * Mendefinisikan status game saat ini.
 * Sangat penting untuk menghentikan update Physics saat game di-pause atau cutscene.
 */
public enum GameplayState {
    INITIALIZING, // Sedang load/build oleh AsyncLoader
    PLAYING,      // normal gameplay
    PAUSED,       // menu pause terbuka
    CUTSCENE,     // sedang memutar dialog/animasi cerita (input player dimatikan)
    GAME_OVER     // pemain mati
}
