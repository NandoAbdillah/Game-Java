package com.bismillahjuara.game.entity;

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
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class SukmaGowong extends Entity {

    public enum State {
        IDLE, CRAWL, RUN, MELEE, DIE, BURNING, DEAD
    }

    private State currentState = State.IDLE;
    private float detectRadius = 15f, runRadius = 5f, meleeRadius = 2.5f;
    private float crawlSpeed = 2f, runSpeed = 6.5f;
    private float stateTimer = 0f, meleeDurationLimit = 4f, burnTimer = 0f;
    private float cullingDistance = 60f;

    private Scene enemyScene;
    private AnimationController animationController;
    private float scale = 0.5f, visualYOffset = 1.0f;
    private boolean isVisible = false;

    // CONSTRUCTOR FINAL: Menerima SceneAsset matang dari GameplayManager!
    public SukmaGowong(Vector3 startPos, GameContext context, SceneAsset asset) {
        super(startPos, context);
        this.collisionRadius = 0.5f;
        this.collisionHeight = 2.0f;
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

    public void takeBurnDamage() {
        if (currentState == State.DEAD || currentState == State.BURNING) return;
        changeState(State.BURNING);
        burnTimer = 3.5f;
        context.audio.playSFX(AudioSFX.ENEMY_BURN);
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
        if (animationController != null) animationController.update(delta);

        Vector3 playerPos = context.player.getPosition();
        float distanceToPlayer = position.dst(playerPos);

        isVisible = (distanceToPlayer < cullingDistance);
        if (!isVisible) return;

        float dirX = playerPos.x - position.x;
        float dirZ = playerPos.z - position.z;
        this.yaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees;

        if (currentState == State.BURNING) {
            burnTimer -= delta;
            this.yaw += MathUtils.random(-20f, 20f);
            if (burnTimer <= 0) finalizeDeath();
        } else if (currentState == State.DIE) {
            stateTimer += delta;
            if (stateTimer >= 2.0f) finalizeDeath();
        } else {
            handleAILogic(distanceToPlayer, dirX, dirZ, delta);
        }

        applyTransform();
    }

    private void finalizeDeath() {
        changeState(State.DEAD);
        if (context.sceneRenderer != null) context.sceneRenderer.removeScene(enemyScene);
        context.audio.playSFX(AudioSFX.ENEMY_DIE);
    }

    private void handleAILogic(float dist, float dirX, float dirZ, float delta) {
        switch (currentState) {
            case IDLE:
                if (dist <= detectRadius && dist > runRadius) changeState(State.CRAWL);
                else if (dist <= runRadius) changeState(State.RUN);
                break;
            case CRAWL:
                moveWithCollision((dirX / dist) * crawlSpeed * delta, (dirZ / dist) * crawlSpeed * delta);
                if (dist <= runRadius) changeState(State.RUN);
                if (dist > detectRadius) changeState(State.IDLE);
                break;
            case RUN:
                moveWithCollision((dirX / dist) * runSpeed * delta, (dirZ / dist) * runSpeed * delta);
                if (dist <= meleeRadius) changeState(State.MELEE);
                break;
            case MELEE:
                context.player.addDanger(delta * 1.5f);
                stateTimer += delta;
                if (stateTimer >= meleeDurationLimit) changeState(State.DIE);
                else if (dist > meleeRadius) changeState(State.RUN);
                break;
            default: break;
        }
    }

    private void changeState(State newState) {
        if (currentState == newState || animationController == null) return;
        this.currentState = newState;
        this.stateTimer = 0f;
        try {
            switch (newState) {
                case IDLE: animationController.animate("Idle", -1, 1f, null, 0.2f); break;
                case CRAWL: animationController.animate("Crawl", -1, 1f, null, 0.2f); break;
                case RUN: animationController.animate("Run", -1, 1f, null, 0.2f); break;
                case MELEE: animationController.animate("Melee", -1, 1f, null, 0.2f); break;
                case DIE: animationController.animate("Die", 1, 1f, null, 0.2f); break;
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

    public void dispose() {
        if (context.sceneRenderer != null && enemyScene != null) context.sceneRenderer.removeScene(enemyScene);
    }
}
