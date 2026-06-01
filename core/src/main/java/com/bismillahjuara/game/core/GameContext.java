package com.bismillahjuara.game.core;

import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.managers.EntityManager;
import com.bismillahjuara.game.managers.WorldManager;
import com.bismillahjuara.game.entity.Player;


public class GameContext {
    public AdvancedCameraSystem camera;
    public GameInputHandler inputHandler;
    public Player player;

    public WorldManager worldManager;
    public EntityManager entityManager;
    public SceneRenderer sceneRenderer;

    public GameplayState state = GameplayState.INITIALIZING;

    public AudioManager audio = AudioManager.getInstance();
    public final float fixedTimeStep = 1f / 60f;


    // STORY & QUEST SYSTEM
    public int currentAct = 1;
    public int relicsCollected = 0;
    public final int RELICS_NEEDED = 3;

    // --- TAMBAHAN PHASE 3: STORY FLOW ---
    public int butoHits = 0;
    public com.bismillahjuara.game.entity.ButoIjo boss = null;
    public boolean isEndingTriggered = false;


    public void collectRelic() {
        relicsCollected++;
        audio.playSFX(com.bismillahjuara.game.audio.AudioSFX.UI_CONFIRM); // Suara pungut

        // Cek Pindah Act
        if (relicsCollected >= RELICS_NEEDED && currentAct == 1) {
            triggerAct2();
        }
    }

    private void triggerAct2() {
        currentAct = 2;
    }
}
