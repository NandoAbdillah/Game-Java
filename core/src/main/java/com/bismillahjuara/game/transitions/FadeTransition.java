package com.bismillahjuara.game.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

/**
 * Efek transisi klasik AAA: Fade to Black.
 * Menggunakan Interpolation.fade agar efek memudarnya terasa natural (tidak linear).
 */
public class FadeTransition implements ScreenTransition {

    private float duration;
    private Texture blackFadeTexture;

    public FadeTransition(float duration) {
        this.duration = duration;

        // OPTIMIZATION: Generate 1x1 white pixel di RAM. Tidak perlu load gambar dari disk!
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackFadeTexture = new Texture(pixmap);
        pixmap.dispose(); // Wajib dispose Pixmap setelah Texture jadi, cegah Memory Leak!
    }

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public void render(SpriteBatch batch, float alpha) {
        // Interpolasi membuat awal lambat, tengah cepat, akhir lambat (smooth cinematic)
        float interpolatedAlpha = Interpolation.fade.apply(alpha);

        batch.begin();
        batch.setColor(1f, 1f, 1f, interpolatedAlpha);

        // Tarik 1 pixel hitam itu untuk menutupi seluruh resolusi layar HP/PC
        batch.draw(blackFadeTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setColor(Color.WHITE); // Reset warna batch (PENTING!)
        batch.end();
    }
}
