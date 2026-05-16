package com.bismillahjuara.game.core;

/**
 * Mendefinisikan status game saat ini.
 * Sangat penting untuk menghentikan update Physics saat game di-pause atau cutscene.
 */
public enum GameplayState {
    INITIALIZING, // Sedang dibangun oleh AsyncLoader
    PLAYING,      // Normal gameplay
    PAUSED,       // Menu pause terbuka
    CUTSCENE,     // Sedang memutar dialog/animasi cerita (Input player dimatikan)
    GAME_OVER     // Pemain mati
}
