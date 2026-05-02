package com.bismillahjuara.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.camera.OrbitCamera;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class Player extends Entity {

    // Konstanta Karakter
    private static final float PLAYER_SPEED = 8f;
    private static final float PLAYER_SPRINT_MULT = 2.2f;
    private static final float PLAYER_HEIGHT = 0f;

    // 3D Model GLTF
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene playerScene;
    private AnimationController animationController;
    private boolean isMoving = false;

    public Player(OrbitCamera camera) {
        super(new Vector3(0, PLAYER_HEIGHT, 0));
        setupGLTF(camera);
    }

    private void setupGLTF(OrbitCamera camera) {
        float skalaKunti = 1.0f;

        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numBones = 60;

        DepthShader.Config depthConfig = PBRShaderProvider.createDefaultDepthConfig();
        depthConfig.numBones = 60;

        sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
        sceneManager.setCamera(camera.getCam());

        sceneManager.setAmbientLight(0.45f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.5f;
        sceneManager.environment.add(sunLight);

        // UBAH PATH FILE SESUAI MILIKMU
        sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/example.glb"));
        playerScene = new Scene(sceneAsset.scene);

        playerScene.modelInstance.transform.setToTranslation(position).scale(skalaKunti, skalaKunti, skalaKunti);

        animationController = new AnimationController(playerScene.modelInstance);

        if (playerScene.modelInstance.animations.size > 0) {
            String namaAnimasiAman = playerScene.modelInstance.animations.get(0).id;
            animationController.animate(namaAnimasiAman, -1, 1f, null, 0.2f);
        }
        animationController.animate("idle", -1, 1f, null, 0.2f);

        sceneManager.addScene(playerScene);
    }

    @Override
    public void update(float delta) {
        // Karena ini Player, updatenya di-drive oleh input, panggil handleMovement dari luar
    }

    /**
     * Memproses logika gerakan matematika
     */
    public void handleMovement(Vector2 moveInput, boolean isSprinting, float camYaw, float delta) {
        boolean currentlyMoving = moveInput.len2() > 0.01f;

        if (currentlyMoving && !isMoving) {
            if (animationController != null) animationController.animate("sprint", -1, 1f, null, 0.2f);
        } else if (!currentlyMoving && isMoving) {
            if (animationController != null) animationController.animate("idle", -1, 1f, null, 0.2f);
        }

        isMoving = currentlyMoving;

        if (currentlyMoving) {
            moveInput.nor();
            float speed = PLAYER_SPEED * (isSprinting ? PLAYER_SPRINT_MULT : 1f);

            float yawRad = MathUtils.degreesToRadians * camYaw;
            float forwardX = -MathUtils.sin(yawRad);
            float forwardZ = -MathUtils.cos(yawRad);
            float rightX   =  MathUtils.cos(yawRad);
            float rightZ   = -MathUtils.sin(yawRad);

            float moveX = (forwardX * moveInput.y + rightX * moveInput.x);
            float moveZ = (forwardZ * moveInput.y + rightZ * moveInput.x);

            position.x += moveX * speed * delta;
            position.z += moveZ * speed * delta;

            yaw = MathUtils.atan2(moveX, moveZ) * MathUtils.radiansToDegrees;

            float mapLimit = 48f;
            position.x = MathUtils.clamp(position.x, -mapLimit, mapLimit);
            position.z = MathUtils.clamp(position.z, -mapLimit, mapLimit);
        }

        position.y = PLAYER_HEIGHT;

        if (playerScene != null) {
            playerScene.modelInstance.transform
                .setToTranslation(position)
                .rotate(Vector3.Y, yaw)
                .scale(1f, 1f, 1f); // Sesuaikan skalanya kembali
        }

        if (animationController != null) animationController.update(delta);
        if (sceneManager != null) sceneManager.update(delta);
    }

    public void render() {
        if (sceneManager != null) sceneManager.render();
    }

    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (sceneAsset != null) sceneAsset.dispose();
    }
}
