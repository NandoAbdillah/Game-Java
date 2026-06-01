package com.bismillahjuara.game.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

public class FadeTransition implements ScreenTransition {

    private float duration;
    private Texture blackFadeTexture;

    public FadeTransition(float duration) {
        this.duration = duration;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackFadeTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public void render(SpriteBatch batch, float alpha) {
        float interpolatedAlpha = Interpolation.fade.apply(alpha);

        batch.begin();
        batch.setColor(1f, 1f, 1f, interpolatedAlpha);

        batch.draw(blackFadeTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setColor(Color.WHITE);
        batch.end();
    }
}
