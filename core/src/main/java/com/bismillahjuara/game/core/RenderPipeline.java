package com.bismillahjuara.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Pembuat visual Game hanya bertugas menggambar, tidak boleh ada perhitungan logic di sini.
 */
public class RenderPipeline {

    private GameContext context;
    private SceneRenderer sceneRenderer;

    public RenderPipeline(GameContext context, SceneRenderer sceneRenderer) {
        this.context = context;
        this.sceneRenderer = sceneRenderer;
    }

    public void render(float delta) {
        // Clear Screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // Render Dunia 3D & Entitas
        if (context.camera != null) {
            // SceneRenderer akan menggambar Map, Musuh, dan Player yang sudah didaftarkan
            sceneRenderer.render(context.camera.getCam(), delta);
        }

        // TODO: Render VFX Particle System di atas 3D Model

        // Render Debug
        // TODO: Buat class DebugRenderer untuk menggambar bounding box (Hitbox) collision
    }
}
