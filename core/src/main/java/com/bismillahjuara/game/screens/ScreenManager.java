package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class ScreenManager {

    private static ScreenManager instance;
    private Game game;
    private SpriteBatch batch;

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


    public void setScreen(Screen screen, com.bismillahjuara.game.transitions.ScreenTransition transition) {
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
                if (currentScreen.getClass() != nextScreen.getClass()) {
                    currentScreen.dispose();
                }
            }
            game.setScreen(nextScreen);
            currentScreen = nextScreen;
        }
    }


    public void render(float delta) {
        if (!isTransitioning) {
            if (currentScreen != null) currentScreen.render(delta);
            return;
        }

        transitionTimer += delta;
        float duration = transition.getDuration();

        float alpha = Math.min(transitionTimer / duration, 1f);

        nextScreen.render(delta);

        transition.render(batch, alpha);

        if (alpha >= 1f) {
            if (currentScreen != null) {
                currentScreen.hide();
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
