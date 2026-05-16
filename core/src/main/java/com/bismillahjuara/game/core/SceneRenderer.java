package com.bismillahjuara.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * Manager Grafis 3D Tertinggi.
 * Menampung semua model (Scene) dan merendernya dengan Shader yang seragam.
 */
public class SceneRenderer {
    private SceneManager sceneManager;

    public SceneRenderer() {
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;
        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        sceneManager = new SceneManager(new DefaultShaderProvider(config), new DepthShaderProvider(depthConfig));

        setupLighting();
    }

    private void setupLighting() {
        sceneManager.setAmbientLight(0.5f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.2f;
        sceneManager.environment.add(sunLight);
    }

    public void addScene(Scene scene) {
        sceneManager.addScene(scene);
    }

    public void removeScene(Scene scene) {
        sceneManager.removeScene(scene);
    }

    public void render(PerspectiveCamera camera, float delta) {
        sceneManager.setCamera(camera);
        sceneManager.update(delta); // Update animasi GLTF
        sceneManager.render();
    }

    public void dispose() {
        sceneManager.dispose();
    }
}
