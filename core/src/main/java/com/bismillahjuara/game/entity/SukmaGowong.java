package com.bismillahjuara.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.core.GameContext;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class SukmaGowong extends Entity {

    // --- ENUM UNTUK STATE MACHINE (AI) ---
    public enum State {
        IDLE, CRAWL, RUN, MELEE, DIE, BURNING, DEAD
    }

    // --- ATRIBUT AI & RADIUS ---
    private State currentState = State.IDLE;
    private float detectRadius = 15f;
    private float runRadius = 5f;
    private float meleeRadius = 2.5f;

    private float crawlSpeed = 2f;
    private float runSpeed = 6.5f;

    private float stateTimer = 0f;
    private float meleeDurationLimit = 4f;

    // --- HORROR LOGIC & CULLING ---
    private boolean isVisible = false;
    private float burnTimer = 0f;
    private float cullingDistance = 60f;

    // FIX TENGGELAM SUKMA GOWONG: Angkat jasadnya agar pas di tanah
    private float visualYOffset = 1.0f; // Sesuaikan angka ini agar pas!

    private SceneAsset sceneAsset;
    private Scene enemyScene;
    private AnimationController animationController;
    private float scale = 0.5f;

    public SukmaGowong(Vector3 startPos, GameContext context) {
        super(startPos, context);
        setupGLTF();
    }

    private void setupGLTF() {
        try {
            sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/SukmaGowong.glb"));
        } catch (Exception e) {
            sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/SukmaGowong.glb"));
        }

        enemyScene = new Scene(sceneAsset.scene);

        for (Material material : enemyScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        // FIX AWAL RENDER: Pasang Offset
        enemyScene.modelInstance.transform
            .setToTranslation(position.x, position.y + visualYOffset, position.z)
            .scale(scale, scale, scale);

        animationController = enemyScene.animationController;
        if (animationController != null) {
            // FIX T-POSE: Kita 'kosongkan' dulu statenya agar animasi Idle tidak di-cancel
            currentState = null;
            changeState(State.IDLE);
        }

        context.sceneRenderer.addScene(enemyScene);
    }

    public void takeBurnDamage() {
        if (currentState == State.DEAD || currentState == State.BURNING) return;

        changeState(State.BURNING);
        burnTimer = 3.5f;
        context.audio.playSFX(com.bismillahjuara.game.audio.AudioSFX.ENEMY_BURN);

        for (Material mat : enemyScene.modelInstance.materials) {
            mat.set(ColorAttribute.createDiffuse(Color.BLACK));
            mat.set(ColorAttribute.createEmissive(Color.FIREBRICK));
        }
    }

    public boolean isDead() {
        return currentState == State.DEAD;
    }

    @Override
    public void update(float delta) {
        if (currentState == State.DEAD) return;

        Vector3 playerPos = context.player.getPosition();
        float distanceToPlayer = position.dst(playerPos);

        isVisible = (distanceToPlayer < cullingDistance);
        if (!isVisible) return;

        float dirX = playerPos.x - position.x;
        float dirZ = playerPos.z - position.z;
        this.yaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees;

        // --- STATE MACHINE LOGIC ---
        if (currentState == State.BURNING) {
            burnTimer -= delta;
            this.yaw += MathUtils.random(-20f, 20f);

            if (burnTimer <= 0) {
                changeState(State.DEAD);
                context.sceneRenderer.removeScene(enemyScene);
                context.audio.playSFX(com.bismillahjuara.game.audio.AudioSFX.ENEMY_DIE);
            }
        } else if (currentState == State.DIE) {
            stateTimer += delta;
            if (stateTimer >= 2.0f) {
                changeState(State.DEAD);
                context.sceneRenderer.removeScene(enemyScene);
            }
        } else {
            switch (currentState) {
                case IDLE:
                    if (distanceToPlayer <= detectRadius && distanceToPlayer > runRadius) {
                        changeState(State.CRAWL);
                    } else if (distanceToPlayer <= runRadius) {
                        changeState(State.RUN);
                    }
                    break;
                case CRAWL:
                    moveWithCollision((dirX / distanceToPlayer) * crawlSpeed * delta, (dirZ / distanceToPlayer) * crawlSpeed * delta);
                    if (distanceToPlayer <= runRadius) changeState(State.RUN);
                    if (distanceToPlayer > detectRadius) changeState(State.IDLE);
                    break;
                case RUN:
                    moveWithCollision((dirX / distanceToPlayer) * runSpeed * delta, (dirZ / distanceToPlayer) * runSpeed * delta);
                    if (distanceToPlayer <= meleeRadius) changeState(State.MELEE);
                    break;
                case MELEE:
                    context.player.addDanger(delta * 1.5f);
                    stateTimer += delta;
                    if (stateTimer >= meleeDurationLimit) {
                        changeState(State.DIE);
                    } else if (distanceToPlayer > meleeRadius) {
                        changeState(State.RUN);
                    }
                    break;
            }
            // Terapkan posisi dan rotasi ke model 3D
            if (enemyScene != null) {
                // FIX UPDATE RENDER: Pasang Offset
                enemyScene.modelInstance.transform
                    .setToTranslation(position.x, position.y + visualYOffset, position.z)
                    .rotate(Vector3.Y, yaw)
                    .scale(scale, scale, scale);
            }
        }

    }

        private void changeState (State newState){
            if (currentState == newState || animationController == null) return;
            this.currentState = newState;
            this.stateTimer = 0f;

            try {
                switch (newState) {
                    case IDLE:
                        animationController.animate("Idle", -1, 1f, null, 0.2f);
                        break;
                    case CRAWL:
                        animationController.animate("Crawl", -1, 1f, null, 0.2f);
                        break;
                    case RUN:
                        animationController.animate("Run", -1, 1f, null, 0.2f);
                        break;
                    case MELEE:
                        animationController.animate("Melee", -1, 1f, null, 0.2f);
                        break;
                    case DIE:
                        animationController.animate("Die", 1, 1f, null, 0.2f);
                        break;
                    case BURNING:
                        animationController.animate("Die", -1, 1f, null, 0.1f);
                        break;
                }
            } catch (Exception e) {
            }
        }

        public void dispose () {
            if (context.sceneRenderer != null && enemyScene != null) {
                context.sceneRenderer.removeScene(enemyScene);
            }
            if (sceneAsset != null) sceneAsset.dispose();
        }
    }

