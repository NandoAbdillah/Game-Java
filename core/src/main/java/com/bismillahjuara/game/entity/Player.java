package com.bismillahjuara.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.camera.OrbitCamera;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
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
    private float skalaKarakter = 4.0f; // jadikan field cla ss


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


        // --- 1. SETUP SHADER (KITA PAKAI DEFAULT SHADER, BUKAN PBR) ---
        // Ini akan mengabaikan hitungan fisika rumit dan langsung memunculkan warna aslimu

        // --- 1. SETUP SHADER (DENGAN KAPASITAS TULANG 80) ---
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;

        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        // WAJIB: masukkan objek config ke dalam kurung Provider!
        sceneManager = new SceneManager(
            new DefaultShaderProvider(config),
            new DepthShaderProvider(depthConfig)
        );

        sceneManager.setCamera(camera.getCam());

        // --- 2. SETUP CAHAYA KLASIK ---
        // Karena bukan PBR, kita butuh ambient yang lebih terang
        sceneManager.setAmbientLight(0.6f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.0f;
        sceneManager.environment.add(sunLight);

        // --- 3. LOAD MODEL ---
        sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/Ava2.glb"));
        playerScene = new Scene(sceneAsset.scene);

        // --- 4. FIX MATERIAL UNTUK SHADER KLASIK ---
        for (Material material : playerScene.modelInstance.materials) {
            // Hapus sifat transparan
            material.remove(BlendingAttribute.Type);
            // Paksa pemotongan kedalaman (agar pohon/lantai tidak tembus)
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            // Paksa bagian dalam baju dibuang agar tidak menumpuk
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));

            // OBAT ANTI ABU-ABU: Paksa warna dasar material menjadi Putih Murni
            // agar warna tekstur gambarmu keluar 100%
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        // --- 5. SET POSISI & SKALA ---
        playerScene.modelInstance.transform
            .setToTranslation(position)
            .scale(skalaKarakter, skalaKarakter, skalaKarakter);

        // --- 6. SETUP ANIMASI ---
        animationController = new AnimationController(playerScene.modelInstance);
        if (playerScene.modelInstance.animations.size > 0) {
            animationController.animate("Idle", -1, 1f, null, 0.2f);
        }

        // --- 7. TAMBAHKAN KE SCENE MANAGER ---
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
            if (animationController != null) animationController.animate("Walk", -1, 1f, null, 0.2f);
        } else if (!currentlyMoving && isMoving) {
            if (animationController != null) animationController.animate("Idle", -1, 1f, null, 0.2f);
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
                .scale(skalaKarakter, skalaKarakter, skalaKarakter); // Sesuaikan skalanya kembali
        }

        if (animationController != null) animationController.update(delta);
        if (sceneManager != null) sceneManager.update(delta);
    }

    public void render() {
        if (sceneManager != null) {
            sceneManager.update(Gdx.graphics.getDeltaTime());
            sceneManager.render();
        }
    }

    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (sceneAsset != null) sceneAsset.dispose();
    }
}
