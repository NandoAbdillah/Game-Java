package com.bismillahjuara.game.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.input.GameInputHandler;

public class HudRenderer {
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GameInputHandler input;

    public HudRenderer(GameInputHandler inputHandler) {
        this.input = inputHandler;
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public void render(Vector3 playerPos, float camYaw) {
        spriteBatch.begin();

        font.draw(spriteBatch,
            String.format("Pos: %.1f, %.1f | Yaw: %.1f", playerPos.x, playerPos.z, camYaw),
            10, Gdx.graphics.getHeight() - 10);

        if (!GameInputHandler.IS_MOBILE) {
            font.draw(spriteBatch, "WASD = Gerak | Shift = Sprint | Drag = Kamera | Scroll = Zoom", 10, 20);
        } else {
            drawVirtualJoystick();
        }

        spriteBatch.end();
    }

    private void drawVirtualJoystick() {
        float cx = input.joystickActive ? input.joystickCenter.x  : Gdx.graphics.getWidth()  * 0.15f;
        float cy = input.joystickActive ? input.joystickCenter.y  : Gdx.graphics.getHeight() * 0.2f;

        font.setColor(new Color(1, 1, 1, 0.6f));
        font.draw(spriteBatch, "[JOYSTICK]", cx - 35, cy);
        if (input.joystickActive) {
            font.draw(spriteBatch, "●", input.joystickCurrent.x - 5, input.joystickCurrent.y);
        }
        font.setColor(Color.WHITE);
    }

    public void resize(int width, int height) {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
    }
}
