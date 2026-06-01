package com.bismillahjuara.game.core;

public class UpdatePipeline {
    private GameContext context;
    private float accumulator = 0f;

    public UpdatePipeline(GameContext context) {
        this.context = context;
    }

    public void update(float delta) {
        if (context.state != GameplayState.PLAYING && context.state != GameplayState.CUTSCENE) {
            return;
        }

        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;

        while (accumulator >= context.fixedTimeStep) {

            if (context.state == GameplayState.PLAYING && context.player != null) {
                context.player.processInputAndPhysics(
                    context.inputHandler.getAction(),
                    context.camera.getYaw(),
                    context.fixedTimeStep
                );
            }

            // update camera tracking
            if (context.player != null) {
                context.camera.update(context.player.getPosition(), context.fixedTimeStep);
            }

            // update peta & ai Entitas (Musuh/NPC)
            context.worldManager.update(context.fixedTimeStep);
            context.entityManager.update(context.fixedTimeStep);

            // Kurangi sisa waktu
            accumulator -= context.fixedTimeStep;
        }
    }
}
