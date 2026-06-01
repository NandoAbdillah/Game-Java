package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Manajer Layar Profesional.
 * Mampu merender dua layar secara bersamaan (Layar A dan Layar B) saat proses transisi.
 */
public class ScreenManager {

    private static ScreenManager instance;
    private Game game;
    private SpriteBatch batch; // Hanya ada 1 SpriteBatch untuk SELURUH game (Batched Rendering Optimization)

    // State Transisi
    private Screen currentScreen;
    private Screen nextScreen;
    private com.bismillahjuara.game.transitions.ScreenTransition transition;
    private float transitionTimer;
    private boolean isTransitioning;

    private ScreenManager() {}

    public static ScreenManager getInstance() {
        if (instance == null) instance = new ScreenManager();
        return instance;
    }

    public void initialize(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
    }

    /**
     * Berpindah layar dengan animasi.
     * @param screen Layar tujuan (misal: MainMenuScreen)
     * @param transition Efek transisi (misal: FadeTransition), bisa null jika ingin instan.
     */
    public void setScreen(Screen screen, com.bismillahjuara.game.transitions.ScreenTransition transition) {
        // Jika sedang transisi, tolak permintaan baru (Anti-Bug / Glitch)
        if (isTransitioning) return;

        this.transition = transition;
        this.nextScreen = screen;

        if (this.transition != null) {
            this.isTransitioning = true;
            this.transitionTimer = 0f;
            this.nextScreen.show();
            this.nextScreen.resize(com.badlogic.gdx.Gdx.graphics.getWidth(), com.badlogic.gdx.Gdx.graphics.getHeight());
        } else {
            if (currentScreen != null) {
                currentScreen.hide();
                // FIX AAA: Jangan dispose jika layarnya tipe yang sama (misal dari GameScreen lama ke GameScreen baru untuk Act 2)
                if (currentScreen.getClass() != nextScreen.getClass()) {
                    currentScreen.dispose();
                }
            }
            game.setScreen(nextScreen);
            currentScreen = nextScreen;
        }
    }

    /** * Wajib dipanggil di dalam method render() milik class Main/Game utama.
     */
    public void render(float delta) {
        if (!isTransitioning) {
            // Normal Render
            if (currentScreen != null) currentScreen.render(delta);
            return;
        }

        // --- SEDANG DALAM PROSES TRANSISI ---
        transitionTimer += delta;
        float duration = transition.getDuration();

        // Hitung persentase (0.0 sampai 1.0)
        float alpha = Math.min(transitionTimer / duration, 1f);

        // 1. Render Layar Tujuan (Di bawah)
        nextScreen.render(delta);

        // 2. Render Efek Transisi (Di atas)
        transition.render(batch, alpha);

        // 3. Cek Selesai Transisi
        if (alpha >= 1f) {
            if (currentScreen != null) {
                currentScreen.hide();
                // FIX AAA: Mencegah JVM Crash (Access Violation) akibat men-dispose VRAM yang sedang di-reuse
                if (currentScreen.getClass() != nextScreen.getClass()) {
                    currentScreen.dispose();
                }
            }
            currentScreen = nextScreen;
            game.setScreen(currentScreen);
            isTransitioning = false;
            transition = null;
        }
    }

    public SpriteBatch getBatch() { return batch; }

    public void dispose() {
        if (batch != null) batch.dispose();
    }
}
