package com.bismillahjuara.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public class WorldMap {
    private SceneManager sceneManager;
    private SceneAsset mapAsset;
    private Scene mapScene;

    public WorldMap() {
        setupGLTFMap();
    }

    private void setupGLTFMap() {
        // --- 1. SETUP SHADER (KITA PAKAI DEFAULT SHADER, BUKAN PBR) ---
        // Samakan dengan karakter: kapasitas tulang 80 agar aman
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;

        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        sceneManager = new SceneManager(
            new DefaultShaderProvider(config),
            new DepthShaderProvider(depthConfig)
        );

        // --- 2. SETUP CAHAYA KLASIK ---
        // Pencahayaan yang terang dan netral
        sceneManager.setAmbientLight(0.6f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.0f;
        sceneManager.environment.add(sunLight);

        // --- 3. LOAD MAP MODEL ---
        // Pastikan path sudah sesuai dengan map milikmu
        mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Map.glb"));
        mapScene = new Scene(mapAsset.scene);

        // --- 4. FIX MATERIAL UNTUK MAP (ANTI ABU-ABU & TEMBUS) ---
        // Looping material persis seperti karakter
        for (Material material : mapScene.modelInstance.materials) {
            // Hapus sifat transparan yang sering bikin glitch di map besar
            material.remove(BlendingAttribute.Type);
            // Paksa pemotongan kedalaman agar tanah dan objek tidak bertumpuk aneh
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            // Buang bagian belakang polygon (CullFace) agar performa naik
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));

            // OBAT ANTI ABU-ABU: Paksa Diffuse ke Putih agar tekstur aslimu terpancar 100%
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        sceneManager.addScene(mapScene);
    }

    public void render(PerspectiveCamera cam) {
        // Render Map 3D
        if (sceneManager != null) {
            sceneManager.setCamera(cam);
            sceneManager.update(Gdx.graphics.getDeltaTime());
            sceneManager.render();
        }
    }

    public void dispose() {
        // Buang memori GLTF secara aman
        if (sceneManager != null) sceneManager.dispose();
        if (mapAsset != null) mapAsset.dispose();
    }
}
