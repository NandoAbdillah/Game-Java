package com.bismillahjuara.game.core;

/**
 * Konduktor Logika Game, Menggunakan sistem Fixed Timestep Accumulator
 */
public class UpdatePipeline {
    private GameContext context;
    private float accumulator = 0f;

    public UpdatePipeline(GameContext context) {
        this.context = context;
    }

    public void update(float delta) {
        if (context.state != GameplayState.PLAYING && context.state != GameplayState.CUTSCENE) {
            return; // berhenti berpikir jika di pause atau mati
        }

        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;

        // FIXED TIMESTEP LOOP (selalu berjalan di 60 FPS )
        while (accumulator >= context.fixedTimeStep) {

            // Baca Input Player (hanya jalan jika state = PLAYING)
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

        // TODO:  Hitung nilai Alpha untuk Render Interpolation:
        // float alpha = accumulator / context.fixedTimeStep;
        // Berikan nilai alpha ini ke RenderPipeline untuk menggeser model dengan super mulus
    }
}
