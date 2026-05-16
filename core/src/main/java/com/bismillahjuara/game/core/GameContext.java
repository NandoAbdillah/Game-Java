package com.bismillahjuara.game.core;

import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.managers.EntityManager;
import com.bismillahjuara.game.managers.WorldManager;
import com.bismillahjuara.game.entity.Player;

/**
 * Jantung Komunikasi Gameplay.
 */
public class GameContext {
    public AdvancedCameraSystem camera;
    public GameInputHandler inputHandler;
    public Player player;

    public WorldManager worldManager;
    public EntityManager entityManager;
    public SceneRenderer sceneRenderer; // FIX: Wajib ada agar entitas bisa mendaftarkan 3D modelnya

    public GameplayState state = GameplayState.INITIALIZING;
    public final float fixedTimeStep = 1f / 60f;
}
