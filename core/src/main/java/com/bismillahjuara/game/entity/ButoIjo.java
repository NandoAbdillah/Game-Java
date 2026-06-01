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
import com.badlogic.gdx.math.collision.BoundingBox;
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

    // --- Skala Raksasa Buto Ijo ---
    private float scale = 2.5f;
    private float visualYOffset = 0.0f;

    // --- AI Parameter ---
    private float moveSpeed = 6.0f;
    private float burnTimer = 0f;
    private float hitFlashTimer = 0f;

    // --- Audio State ---
    private float footstepTimer = 0f;
    private float monsterRoarTimer = 0f;
    private final float ROAR_DISTANCE = 25f;

    public ButoIjo(Vector3 startPos, GameContext context, SceneAsset asset) {
        super(startPos, context);

        this.collisionRadius = 2.0f;
        this.collisionHeight = 7.0f;

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
        hitFlashTimer = 0.2f;
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

        if (animationController != null) {
            animationController.update(delta);
        }

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
            return; // Hentikan logika pergerakan saat terbakar
        }

        Vector3 playerPos = context.player.getPosition();
        float dist = position.dst(playerPos);
        float dirX = playerPos.x - position.x;
        float dirZ = playerPos.z - position.z;

        float targetYaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees;
        this.yaw = lerpAngle(this.yaw, targetYaw, 5.0f * delta);

        if (dist <= ROAR_DISTANCE) {
            monsterRoarTimer -= delta;
            if (monsterRoarTimer <= 0) {
                context.audio.playSFX(AudioSFX.ENEMY_SCREAM);
                monsterRoarTimer = MathUtils.random(4f, 7f);
            }
        } else {
            monsterRoarTimer = 0f;
        }

        if (dist > 3.0f) {
            if (currentState != State.CHASE) {
                changeState(State.CHASE);
            }

            moveIgnoreTrees((dirX / dist) * moveSpeed * delta, (dirZ / dist) * moveSpeed * delta);

            footstepTimer -= delta;
            if (footstepTimer <= 0) {
                context.audio.playRandomFootstep(true);
                footstepTimer = 0.5f;
            }
        } else {
            if (currentState != State.IDLE) {
                changeState(State.IDLE);
            }
        }

        applyTransform();
    }

    private void moveIgnoreTrees(float stepX, float stepZ) {
        if (context == null || context.worldManager == null) return;

        Vector3 nextPos = new Vector3(position.x + stepX, position.y, position.z + stepZ);
        float safeMinX = context.worldManager.mapBounds.min.x + context.worldManager.safePlayMargin;
        float safeMaxX = context.worldManager.mapBounds.max.x - context.worldManager.safePlayMargin;
        float safeMinZ = context.worldManager.mapBounds.min.z + context.worldManager.safePlayMargin;
        float safeMaxZ = context.worldManager.mapBounds.max.z - context.worldManager.safePlayMargin;

        if (nextPos.x - collisionRadius < safeMinX || nextPos.x + collisionRadius > safeMaxX ||
            nextPos.z - collisionRadius < safeMinZ || nextPos.z + collisionRadius > safeMaxZ) {
            return;
        }

        BoundingBox myBox = new BoundingBox(
            new Vector3(nextPos.x - collisionRadius, nextPos.y, nextPos.z - collisionRadius),
            new Vector3(nextPos.x + collisionRadius, nextPos.y + collisionHeight, nextPos.z + collisionRadius)
        );

        boolean hitBuilding = false;
        for (Entity e : context.entityManager.getEntities()) {
            if (e instanceof EnvironmentProp) {
                if (nextPos.dst(e.getPosition()) < 10f) {
                    if (nextPos.dst(e.getPosition()) < 5.0f) {
                        hitBuilding = true;
                        break;
                    }
                }
            }
        }

        if (!hitBuilding) {
            position.x += stepX;
            position.z += stepZ;
        }
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
                case CHASE: animationController.animate("Run", -1, 1f, null, 0.2f); break;
                case BURNING: animationController.animate("Die", -1, 1f, null, 0.1f); break;
                default: break;
            }
        } catch (Exception e) {
            Gdx.app.error("BUTO_IJO", "Gagal play animasi: " + newState.name(), e);
        }
    }

    private float lerpAngle(float current, float target, float speed) {
        float diff = target - current;
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;
        return current + diff * speed;
    }

    private void applyTransform() {
        if (enemyScene != null) {
            enemyScene.modelInstance.transform.setToTranslation(position.x, position.y + visualYOffset, position.z)
                .rotate(Vector3.Y, yaw)
                .scale(scale, scale, scale);
        }
    }
}
