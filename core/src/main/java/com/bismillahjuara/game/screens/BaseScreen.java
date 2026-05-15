package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Blueprint abstrak untuk seluruh Screen di game ini.
 * Menangani boilerplate code seperti Stage, Viewport, dan Clear Color.
 */
public abstract class BaseScreen implements Screen {

    protected Stage stage;
    protected Viewport viewport;

    public BaseScreen() {
        // FitViewport menjamin aspek rasio UI tidak akan pernah gepeng (stretching),
        // sangat penting untuk logo studio dan UI Mobile! (Standard: 1920x1080)
        viewport = new FitViewport(1920, 1080);

        // Memakai SpriteBatch global dari ScreenManager agar optimal
        stage = new Stage(viewport, ScreenManager.getInstance().getBatch());
    }

    @Override
    public void show() {
        // Menjadikan Stage ini sebagai penangkap input (untuk tombol UI nantinya)
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Pembersihan layar dengan warna solid (Hitam)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Update dan gambar semua animasi UI di dalam Stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Penting: Parameter 'true' membuat kamera langsung ke tengah layar
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
