package com.bismillahjuara.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class ButoIjo extends Entity {

    public enum State { IDLE, CHASE, BURNING, DEAD }

    private State currentState = State.IDLE;
    private Scene enemyScene;
    private AnimationController animationController;

    private int hitsTaken = 0;
    private float scale = 1.2f; // Boss Size (Lebih besar)
    private float visualYOffset = 0.0f;

    private float moveSpeed = 4.0f; // Boss Jalan perlahan tapi pasti
    private float burnTimer = 0f;
    private float footstepTimer = 0f;
    private float hitFlashTimer = 0f;

    public ButoIjo(Vector3 startPos, GameContext context, SceneAsset asset) {
        super(startPos, context);
        this.collisionRadius = 1.0f;
        this.collisionHeight = 3.5f;

        // DEBUG WAJIB: Log semua animasi yang tersedia di model!
        Gdx.app.log("BUTO_IJO_ANIM", "=== DAFTAR ANIMASI TERSEDIA ===");
        for (Animation anim : asset.scene.model.animations) {
            Gdx.app.log("BUTO_IJO_ANIM", "Nama: [" + anim.id + "] | Durasi: " + anim.duration + " dtk");
        }
        Gdx.app.log("BUTO_IJO_ANIM", "===============================");

        setupVisuals(asset);
    }

    private void setupVisuals(SceneAsset asset) {
        enemyScene = new Scene(asset.scene);
        for (Material material : enemyScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        applyTransform();
        animationController = enemyScene.animationController;
        if (animationController != null) {
            currentState = null;
            changeState(State.IDLE);
        }
        if (context.sceneRenderer != null) context.sceneRenderer.addScene(enemyScene);
    }

    public void takeHit() {
        if (currentState == State.DEAD || currentState == State.BURNING) return;

        hitsTaken++;
        context.butoHits = hitsTaken;
        hitFlashTimer = 0.2f; // Kedip merah 0.2 detik
        context.audio.playSFX(AudioSFX.ENEMY_BURN);

        if (hitsTaken >= 10) {
            changeState(State.BURNING);
            burnTimer = 3.5f;
            for (Material mat : enemyScene.modelInstance.materials) {
                mat.set(ColorAttribute.createDiffuse(Color.BLACK));
                mat.set(ColorAttribute.createEmissive(Color.FIREBRICK));
            }
        }
    }

    public boolean isDead() {
        return currentState == State.DEAD || currentState == State.BURNING;
    }

    @Override
    public void update(float delta) {
        if (currentState == State.DEAD) return;
        if (animationController != null) animationController.update(delta);

        // Logic efek kedip merah saat dipukul
        if (hitFlashTimer > 0 && currentState != State.BURNING) {
            hitFlashTimer -= delta;
            for (Material mat : enemyScene.modelInstance.materials) {
                mat.set(ColorAttribute.createDiffuse(hitFlashTimer > 0 ? Color.RED : Color.WHITE));
            }
        }

        if (currentState == State.BURNING) {
            burnTimer -= delta;
            this.yaw += MathUtils.random(-20f, 20f);
            if (burnTimer <= 0) finalizeDeath();
            applyTransform();
            return;
        }

        Vector3 playerPos = context.player.getPosition();
        float dist = position.dst(playerPos);
        float dirX = playerPos.x - position.x;
        float dirZ = playerPos.z - position.z;
        this.yaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees;

        // Boss selalu mengejar perlahan (Terminator style)
        if (dist > 1.5f) {
            changeState(State.CHASE);
            moveWithCollision((dirX / dist) * moveSpeed * delta, (dirZ / dist) * moveSpeed * delta);

            // Audio Footstep Dummy
            footstepTimer -= delta;
            if (footstepTimer <= 0) {
                context.audio.playRandomFootstep(true);
                footstepTimer = 0.6f; // Langkah berat bos
            }
        } else {
            changeState(State.IDLE);
        }

        applyTransform();
    }

    private void finalizeDeath() {
        currentState = State.DEAD;
        if (context.sceneRenderer != null) context.sceneRenderer.removeScene(enemyScene);
        context.audio.playSFX(AudioSFX.ENEMY_DIE);
    }

    private void changeState(State newState) {
        if (currentState == newState || animationController == null) return;
        this.currentState = newState;
        try {
            switch (newState) {
                case IDLE: animationController.animate("Idle", -1, 1f, null, 0.2f); break;
                // Asumsi boss pakai model/animasi sama sementara. Cek log "BUTO_IJO_ANIM" nanti!
                case CHASE: animationController.animate("Run", -1, 1f, null, 0.2f); break;
                case BURNING: animationController.animate("Die", -1, 1f, null, 0.1f); break;
                default: break;
            }
        } catch (Exception e) {}
    }

    private void applyTransform() {
        if (enemyScene != null) {
            enemyScene.modelInstance.transform.setToTranslation(position.x, position.y + visualYOffset, position.z).rotate(Vector3.Y, yaw).scale(scale, scale, scale);
        }
    }
}
