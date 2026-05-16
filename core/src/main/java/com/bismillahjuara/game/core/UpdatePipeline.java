package com.bismillahjuara.game.core;

/**
 * Konduktor Logika Game (Physics, AI, Movement).
 * Menggunakan sistem Fixed Timestep Accumulator untuk menjamin konsistensi Fisika.
 */
public class UpdatePipeline {
    private GameContext context;
    private float accumulator = 0f;

    public UpdatePipeline(GameContext context) {
        this.context = context;
    }

    public void update(float delta) {
        if (context.state != GameplayState.PLAYING && context.state != GameplayState.CUTSCENE) {
            return; // Berhenti berpikir jika di-pause atau mati
        }

        // Failsafe: Jika game nge-lag parah (misal ketahan loading), jangan lakukan "Spiral of Death"
        float frameTime = Math.min(delta, 0.25f);
        accumulator += frameTime;

        // FIXED TIMESTEP LOOP (Selalu berjalan di 60 FPS Logic)
        while (accumulator >= context.fixedTimeStep) {

            // 1. Baca Input Player (Hanya jalan jika state = PLAYING)
            if (context.state == GameplayState.PLAYING && context.player != null) {
                // Di masa depan, ini dipecah. Untuk sekarang kita pakai method milik Player
                context.player.processInputAndPhysics(
                    context.inputHandler.getAction(),
                    context.camera.getYaw(),
                    context.fixedTimeStep
                );
            }

            // 2. Update Camera Tracking
            if (context.player != null) {
                context.camera.update(context.player.getPosition(), context.fixedTimeStep);
            }

            // 3. Update Peta & AI Entitas (Musuh/NPC)
            context.worldManager.update(context.fixedTimeStep);
            context.entityManager.update(context.fixedTimeStep);

            // Kurangi sisa waktu
            accumulator -= context.fixedTimeStep;
        }

        // TODO: (Future/Phase 5) Hitung nilai Alpha untuk Render Interpolation:
        // float alpha = accumulator / context.fixedTimeStep;
        // Berikan nilai alpha ini ke RenderPipeline untuk menggeser model dengan super mulus.
    }
}
