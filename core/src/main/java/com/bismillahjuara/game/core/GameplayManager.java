package com.bismillahjuara.game.core;

import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.entity.Player;
import com.bismillahjuara.game.entity.SukmaGowong;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.managers.EntityManager;
import com.bismillahjuara.game.managers.WorldManager;
import com.badlogic.gdx.math.Vector3;

public class GameplayManager {

    private GameContext context;

    private UpdatePipeline updatePipeline;
    private RenderPipeline renderPipeline;

    public GameplayManager() {
        context = new GameContext();
        context.sceneRenderer = new SceneRenderer(); // Instansiasi lebih dulu

        context.worldManager = new WorldManager(context);
        context.entityManager = new EntityManager(context);

        updatePipeline = new UpdatePipeline(context);
        renderPipeline = new RenderPipeline(context, context.sceneRenderer);
    }

    public void buildWorld() {
        context.worldManager.initialize(context.sceneRenderer);
    }

    public void buildEntities(AdvancedCameraSystem camera) {
        context.camera = camera;

        // Sekarang Player masuk dengan Context. Player akan otomatis registrasi modelnya ke sceneRenderer
        context.player = new Player(context);

        // TODO: Update SukmaGowong agar mirip Player, bisa register ke context.sceneRenderer
    }

    public void bindInput(GameInputHandler inputHandler) {
        context.inputHandler = inputHandler;
    }

    public void startGameplay() {
        context.state = GameplayState.PLAYING;
    }

    public void update(float delta) {
        updatePipeline.update(delta);
    }

    public void render(float delta) {
        renderPipeline.render(delta);
    }

    public void dispose() {
        context.sceneRenderer.dispose();
        context.worldManager.dispose();
        context.entityManager.dispose();
        if (context.player != null) context.player.dispose();
    }
}
