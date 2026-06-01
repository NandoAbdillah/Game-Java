package com.bismillahjuara.game.transitions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface ScreenTransition {

    float getDuration();

    void render(SpriteBatch batch, float alpha);
}
