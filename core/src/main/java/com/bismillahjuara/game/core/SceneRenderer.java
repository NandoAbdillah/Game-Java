package com.bismillahjuara.game.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.bismillahjuara.game.world.SkyEnvironmentSystem;

import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public class SceneRenderer {

    private SceneManager sceneManager;
    private DirectionalLightEx mainSunLight;

    private SkyEnvironmentSystem skySystem;

    public SceneRenderer() {
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;
        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        sceneManager = new SceneManager(new DefaultShaderProvider(config), new DepthShaderProvider(depthConfig));

        setupLighting();

        skySystem = new SkyEnvironmentSystem(sceneManager, mainSunLight);
    }

    private void setupLighting() {
        sceneManager.setAmbientLight(0.5f); // Akan di-override oleh SkySystem
        mainSunLight = new DirectionalLightEx();
        mainSunLight.direction.set(-1f, -1f, -0.4f).nor();
        mainSunLight.color.set(Color.WHITE);
        mainSunLight.intensity = 1.2f;
        sceneManager.environment.add(mainSunLight);
    }

    public Environment getEnvironment() {
        return sceneManager.environment;
    }

    public void addScene(Scene scene) {
        sceneManager.addScene(scene);
    }

    public void removeScene(Scene scene) {
        sceneManager.removeScene(scene);
    }

    public void render(PerspectiveCamera camera, float delta) {
        skySystem.update(delta, camera.position);
        skySystem.render(camera);

        sceneManager.setCamera(camera);
        sceneManager.update(delta);
        sceneManager.render();
    }

    public void dispose() {
        sceneManager.dispose();
        if (skySystem != null) skySystem.dispose();
    }
}
