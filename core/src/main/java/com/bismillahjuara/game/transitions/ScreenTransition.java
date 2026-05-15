package com.bismillahjuara.game.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Interface dasar untuk semua efek transisi layar.
 * Memastikan arsitektur ini Scalable untuk transisi jenis apapun di masa depan.
 */
public interface ScreenTransition {

    /** Mengembalikan durasi total transisi dalam detik. */
    float getDuration();

    /** * Dirender di atas dua layar yang sedang bertransisi.
     * @param batch SpriteBatch global (di-reuse agar optimal)
     * @param currScreen Layar yang akan ditinggalkan
     * @param nextScreen Layar tujuan
     * @param alpha Nilai 0.0 (Mulai) ke 1.0 (Selesai)
     */
    void render(SpriteBatch batch, float alpha);
}
