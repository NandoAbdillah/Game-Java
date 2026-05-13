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
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public class Player extends Entity {

    // --- ENUM UNTUK STATE MACHINE (Animasi) ---
    public enum State {
        IDLE, WALK, RUN, JUMP, THROW, DYING
    }

    // Konstanta Karakter
    private static final float PLAYER_SPEED = 8f;
    private static final float PLAYER_SPRINT_MULT = 2.2f;
    private static final float PLAYER_HEIGHT = 0f;
    private float skalaKarakter = 4.0f; // jadikan field class

    // --- ATRIBUT LOMPAT & GRAVITASI SEDERHANA ---
    private float verticalVelocity = 0f;
    private static final float GRAVITY = -30f; // Tarikan gravitasi ke bawah
    private static final float JUMP_POWER = 12f; // Kekuatan lompat ke atas

    // 3D Model GLTF
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene playerScene;
    private AnimationController animationController;

    // Menggantikan "boolean isMoving"
    private State currentState = State.IDLE;

    public Player(OrbitCamera camera) {
        super(new Vector3(0, PLAYER_HEIGHT, 0));
        setupGLTF(camera);
    }

    private void setupGLTF(OrbitCamera camera) {
        // --- 1. SETUP SHADER (DEFAULT SHADER ANTI PUCAT) ---
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;

        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        sceneManager = new SceneManager(
            new DefaultShaderProvider(config),
            new DepthShaderProvider(depthConfig)
        );

        sceneManager.setCamera(camera.getCam());

        // --- 2. SETUP CAHAYA ---
        sceneManager.setAmbientLight(0.6f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.0f;
        sceneManager.environment.add(sunLight);

        // --- 3. LOAD MODEL ---
        sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/TimunAnim.glb"));
        playerScene = new Scene(sceneAsset.scene);

        // =================================================================
        // --- KODE PELACAK NAMA ANIMASI SUPER MENCOLOK ---
        // =================================================================
        Gdx.app.log("CEK_ANIMASI", "=======================================");
        Gdx.app.log("CEK_ANIMASI", "Mencari animasi di dalam TimunAnim.glb...");

        // Membaca langsung dari sceneAsset yang menyimpan master datanya
        if (sceneAsset.animations == null || sceneAsset.animations.size == 0) {
            Gdx.app.log("CEK_ANIMASI", "GAWAT! Tidak ada animasi yang ter-export di file ini!");
            Gdx.app.log("CEK_ANIMASI", "Penyebab: Kamu lupa klik 'Push Down' ke NLA Track di Blender!");
        } else {
            for (int i = 0; i < sceneAsset.animations.size; i++) {
                Gdx.app.log("CEK_ANIMASI", "Animasi Tersedia -> '" + sceneAsset.animations.get(i).id + "'");
            }
        }
        Gdx.app.log("CEK_ANIMASI", "=======================================");
        // =================================================================

        // --- 4. FIX MATERIAL ---
        for (Material material : playerScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        // --- 5. SET POSISI & SKALA ---
        playerScene.modelInstance.transform
            .setToTranslation(position)
            .scale(skalaKarakter, skalaKarakter, skalaKarakter);

        // --- 6. SETUP ANIMASI ---
        animationController = new AnimationController(playerScene.modelInstance);
        changeState(State.IDLE); // Memanggil method pengatur state animasi

        // --- 7. TAMBAHKAN KE SCENE MANAGER ---
        sceneManager.addScene(playerScene);
    }

    @Override
    public void update(float delta) {
        // Karena ini Player, updatenya di-drive oleh input, panggil handleMovement dari luar
    }

    // =======================================================
    // --- METHOD AKSI (Dipanggil dari Input / GameScreen) ---
    // =======================================================

    public void jump() {
        if (position.y <= PLAYER_HEIGHT && currentState != State.DYING && currentState != State.THROW) {
            verticalVelocity = JUMP_POWER;
            changeState(State.JUMP);
        }
    }

    public void throwItem() {
        if (currentState != State.DYING && currentState != State.JUMP) {
            changeState(State.THROW);
        }
    }

    public void die() {
        if (currentState != State.DYING) {
            changeState(State.DYING);
        }
    }

    private void changeState(State newState) {
        if (currentState == newState) return;
        if (currentState == State.DYING) return;

        this.currentState = newState;

        try {
            // NANTI GANTI NAMA DI DALAM TANDA KUTIP INI
            // SESUAI DENGAN LOG CAT YANG MUNCUL DARI "CEK_ANIMASI" YA!
            switch (newState) {
                case IDLE:    animationController.animate("Idle", -1, 1f, null, 0.2f); break;
                case WALK:    animationController.animate("Walk", -1, 1f, null, 0.2f); break;
                case RUN:     animationController.animate("Run", -1, 1f, null, 0.2f); break;
                case JUMP:
                    animationController.animate("Jump", 1, 1f, null, 0.2f);
                    break;
                case THROW:
                    animationController.animate("Throw", 1, 1f, new AnimationController.AnimationListener() {
                        @Override
                        public void onEnd(AnimationController.AnimationDesc animation) {
                            currentState = State.IDLE;
                            animationController.animate("Idle", -1, 1f, null, 0.2f);
                        }
                        @Override public void onLoop(AnimationController.AnimationDesc animation) {}
                    }, 0.2f);
                    break;
                case DYING:
                    animationController.animate("Dying", 1, 1f, null, 0.2f);
                    break;
            }
        } catch (Exception e) {
            Gdx.app.log("CEK_ANIMASI", "Warning: Animasi untuk state " + newState + " tidak ditemukan!");
        }
    }

    public void handleMovement(Vector2 moveInput, boolean isSprinting, float camYaw, float delta) {
        if (currentState == State.DYING) return;

        // --- 1. LOGIKA GRAVITASI & LOMPAT ---
        verticalVelocity += GRAVITY * delta;
        position.y += verticalVelocity * delta;

        boolean isGrounded = position.y <= PLAYER_HEIGHT;
        if (isGrounded) {
            position.y = PLAYER_HEIGHT;
            verticalVelocity = 0f;
            if (currentState == State.JUMP) {
                changeState(State.IDLE);
            }
        }

        // --- 2. LOGIKA ANIMASI BERJALAN/LARI ---
        boolean currentlyMoving = moveInput.len2() > 0.01f;

        if (currentState != State.JUMP && currentState != State.THROW) {
            if (currentlyMoving) {
                changeState(isSprinting ? State.RUN : State.WALK); // FIX: Tadinya RUN : RUN, kuubah jadi WALK
            } else {
                changeState(State.IDLE);
            }
        }

        // --- 3. KALKULASI PERPINDAHAN POSISI ---
        if (currentlyMoving && currentState != State.THROW) {
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

        // --- 4. TERAPKAN KE MODEL 3D ---
        if (playerScene != null) {
            playerScene.modelInstance.transform
                .setToTranslation(position)
                .rotate(Vector3.Y, yaw)
                .scale(skalaKarakter, skalaKarakter, skalaKarakter);
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
