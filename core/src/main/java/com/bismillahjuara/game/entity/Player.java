package com.bismillahjuara.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.input.InputAction;
import com.bismillahjuara.game.audio.AudioSFX;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Player extends Entity {

    public enum State {
        IDLE(0), WALK(0), RUN(0), CROUCH_IDLE(0), CROUCH_WALK(0), COMBAT_IDLE(0),
        JUMP(1), JUMP_RUN(1), THROW(2), KICK(2), COMBAT(2), EMOTE(2), HEAL(2), DYING(99);
        public final int priority;
        State(int priority) { this.priority = priority; }
    }

    private static final float SPEED_WALK = 6f, SPEED_RUN = 14f, SPEED_CROUCH = 3f;
    private static final float ACCELERATION = 10f, DECELERATION = 15f, ROTATION_SMOOTH = 15f;
    private static final float GRAVITY = -35f, JUMP_POWER = 14f, PLAYER_HEIGHT = 0f;

    private Vector2 currentVelocity2D = new Vector2();
    private float verticalVelocity = 0f;
    private State currentState = State.IDLE;
    private String currentAnimName = "";

    private boolean isCrouching = false, isCombatMode = false;
    private float combatModeTimer = 0f, bufferTimer = 0f;
    private State bufferedState = null;

    private Scene playerScene;
    private AnimationController animationController;
    private float skalaKarakter = 4.0f, visualYOffset = 0.0f;

    public float health = 100f;
    public float dangerTimer = 0f;

    private PointLightEx auraLight;
    private float healGlowTimer = 0f;

    private float footstepTimer = 0f;
    private float lastThrowTime = 0f;
    private static final float THROW_COOLDOWN = 5.0f;

    // --- FIX AAA: Heartbeat System ---
    private float heartbeatDurationTimer = 0f;
    private float heartbeatPulseTimer = 0f;

    public Player(GameContext context, SceneAsset asset) {
        super(new Vector3(0, PLAYER_HEIGHT, 0), context);
        this.collisionRadius = 1.0f;
        this.collisionHeight = 4.0f;
        setupVisuals(asset);
        setupAuraLight();
    }

    private void setupVisuals(SceneAsset asset) {
        playerScene = new Scene(asset.scene);
        for (Material material : playerScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
        }
        applyTransform();
        animationController = playerScene.animationController;
        if (animationController != null) changeState(State.IDLE, true);
        if (context.sceneRenderer != null) context.sceneRenderer.addScene(playerScene);
    }

    private void setupAuraLight() {
        auraLight = new PointLightEx();
        auraLight.color.set(0.2f, 0.8f, 0.8f, 1f);
        auraLight.intensity = 1.5f;
        context.sceneRenderer.getEnvironment().add(auraLight);
    }

    public void takeDamage(float amount) {
        if (currentState == State.DYING) return;
        health -= amount;

        // Reset timer heartbeat, jantung berdebar selama 3 detik setelah terkena hit
        heartbeatDurationTimer = 3.0f;

        if (health <= 0) {
            health = 0;
            heartbeatDurationTimer = 0f; // Matikan jantung saat game over
            changeState(State.DYING, true);
            context.state = com.bismillahjuara.game.core.GameplayState.GAME_OVER;
        }
    }

    @Override
    public void update(float delta) {
        if (animationController != null) animationController.update(delta);
    }

    public void processInputAndPhysics(InputAction input, float camYaw, float delta) {
        if (currentState == State.DYING) return;

        if (lastThrowTime > 0) lastThrowTime -= delta;

        // --- UPDATE HEARTBEAT AUDIO ---
        if (heartbeatDurationTimer > 0) {
            heartbeatDurationTimer -= delta;
            heartbeatPulseTimer -= delta;
            if (heartbeatPulseTimer <= 0) {

                context.audio.playSFX(AudioSFX.HEARTBEAT);
                heartbeatPulseTimer = 0.8f;
            }
        }

        // --- UPDATE LAMPU SENTER / HEAL ---
        if (healGlowTimer > 0) {
            healGlowTimer -= delta;
            float progress = healGlowTimer / 2.0f;
            auraLight.color.set(
                MathUtils.lerp(0.2f, 0.2f, progress),
                MathUtils.lerp(0.8f, 1.0f, progress),
                MathUtils.lerp(0.8f, 0.2f, progress), 1f
            );
            auraLight.intensity = MathUtils.lerp(1.5f, 4.0f, progress);
        } else {
            auraLight.color.set(0.2f, 0.8f, 0.8f, 1f);
            auraLight.intensity = 1.5f;
        }

        auraLight.position.set(position.x, position.y + 2.5f, position.z);

        if (input.toggleCameraPressed) context.camera.toggleMode();

        if (input.diePressed) {
            health = 0;
            changeState(State.DYING, true);
            context.state = com.bismillahjuara.game.core.GameplayState.GAME_OVER;
            return;
        }

        if (input.healPressed && healGlowTimer <= 0) {
            requestState(State.HEAL);
            healGlowTimer = 2.0f;
            health = Math.min(health + 30f, 100f);
        }

        if (input.emotePressed) requestState(State.EMOTE);

        if (input.throwPressed && lastThrowTime <= 0) {
            throwBiji();
            requestState(State.THROW);
            lastThrowTime = THROW_COOLDOWN;
        }

        if (input.kickPressed) {
            performMeleeAttack(34f);
            requestState(State.KICK);
        }
        if (input.attackPressed) {
            performMeleeAttack(34f);
            requestState(State.COMBAT);
        }

        if (input.jumpPressed) requestState(input.sprintHeld && (input.moveX != 0 || input.moveY != 0) ? State.JUMP_RUN : State.JUMP);
        if (input.crouchToggled && currentState.priority == 0) { isCrouching = !isCrouching; isCombatMode = false; }

        if (bufferTimer > 0) bufferTimer -= delta; else bufferedState = null;
        if (isCombatMode && currentState.priority == 0) {
            combatModeTimer -= delta;
            if (combatModeTimer <= 0) isCombatMode = false;
        }

        verticalVelocity += GRAVITY * delta;
        position.y += verticalVelocity * delta;
        boolean isGrounded = position.y <= PLAYER_HEIGHT;
        if (isGrounded) {
            position.y = PLAYER_HEIGHT;
            verticalVelocity = 0f;
            if (currentState == State.JUMP || currentState == State.JUMP_RUN) actionFinished();
        }

        boolean hasInput = (Math.abs(input.moveX) > 0.1f || Math.abs(input.moveY) > 0.1f);
        Vector2 targetVel = new Vector2();

        if (currentState.priority == 0 || !isGrounded) {
            if (hasInput) {
                float targetSpeed = isCrouching || input.crouchHeld ? SPEED_CROUCH : (input.sprintHeld && !isCombatMode ? SPEED_RUN : SPEED_WALK);
                State locState = isCrouching || input.crouchHeld ? State.CROUCH_WALK : (input.sprintHeld && !isCombatMode ? State.RUN : State.WALK);
                float yawRad = MathUtils.degreesToRadians * camYaw;
                targetVel.set((-MathUtils.sin(yawRad) * input.moveY + MathUtils.cos(yawRad) * input.moveX) * targetSpeed,
                    (-MathUtils.cos(yawRad) * input.moveY - MathUtils.sin(yawRad) * input.moveX) * targetSpeed);
                if (isGrounded) changeState(locState, false);
                yaw = lerpAngle(yaw, MathUtils.atan2(targetVel.x, targetVel.y) * MathUtils.radiansToDegrees, ROTATION_SMOOTH * delta);
            } else {
                if (isGrounded) changeState((isCrouching || input.crouchHeld) ? State.CROUCH_IDLE : State.IDLE, false);
            }
        }

        float accelRate = hasInput ? ACCELERATION : DECELERATION;
        currentVelocity2D.lerp(targetVel, (!isGrounded ? accelRate * 0.2f : accelRate) * delta);
        moveWithCollision(currentVelocity2D.x * delta, currentVelocity2D.y * delta);

        applyTransform();
        handleFootstepAudio(delta, isGrounded, hasInput);
    }

    private void performMeleeAttack(float damage) {
        for (Entity e : context.entityManager.getEntities()) {
            if (e instanceof SukmaGowong) {
                SukmaGowong enemy = (SukmaGowong) e;
                if (!enemy.isDead() && position.dst(enemy.getPosition()) < 3.0f) {
                    enemy.takeDamage(damage);
                }
            }
        }
    }

    private void handleFootstepAudio(float delta, boolean isGrounded, boolean hasInput) {
        if (isGrounded && hasInput && (currentState == State.WALK || currentState == State.RUN || currentState == State.CROUCH_WALK)) {
            footstepTimer -= delta;
            if (footstepTimer <= 0) {
                context.audio.playRandomFootstep(true);
                footstepTimer = currentState == State.RUN ? 0.28f : (currentState == State.CROUCH_WALK ? 0.65f : 0.45f);
            }
        } else if (!hasInput || !isGrounded) {
            footstepTimer = 0f;
            context.audio.stopFootstep();
        }
    }

    private void throwBiji() {
        float yawRad = MathUtils.degreesToRadians * yaw;
        float spawnX = position.x + (MathUtils.sin(yawRad) * 1.5f);
        float spawnZ = position.z + (MathUtils.cos(yawRad) * 1.5f);

        for (int i = 0; i < 15; i++) {
            context.entityManager.addEntity(new BijiTimunProjectile(
                new Vector3(spawnX, position.y + 2.5f, spawnZ),
                yaw + MathUtils.random(-35f, 35f),
                context,
                (i == 0)
            ));
        }
    }

    private void requestState(State requested) {
        if (currentState == State.DYING) return;
        if (requested.priority >= currentState.priority) {
            if ((requested == State.JUMP || requested == State.JUMP_RUN) && position.y > PLAYER_HEIGHT + 0.1f) return;
            if (requested == State.JUMP || requested == State.JUMP_RUN) { verticalVelocity = JUMP_POWER; isCrouching = false; }
            if (requested == State.COMBAT || requested == State.KICK) { isCombatMode = true; combatModeTimer = 5f; isCrouching = false; }
            changeState(requested, false);
        } else {
            bufferedState = requested;
            bufferTimer = 0.3f;
        }
    }

    private void changeState(State newState, boolean force) {
        if (!force && currentState == newState && newState.priority < 2) return;
        this.currentState = newState;
        String anim = ""; float time = 0.15f; int loops = -1;
        switch (newState) {
            case IDLE: anim = isCombatMode ? "CombatIdle" : "Idle"; break;
            case WALK: anim = isCombatMode ? "SneakWalk" : "Walk"; break;
            case RUN: anim = "Run"; break;
            case CROUCH_IDLE: anim = "CrouchIdle"; break;
            case CROUCH_WALK: anim = "CrouchWalk"; break;
            case JUMP: anim = "Jump"; loops = 1; time = 0.1f; break;
            case JUMP_RUN: anim = "JumpRun"; loops = 1; time = 0.1f; break;
            case COMBAT: anim = "Combat"; loops = 1; time = 0.05f; break;
            case KICK: anim = "Kick"; loops = 1; time = 0.05f; break;
            case THROW: anim = "Throw"; loops = 1; time = 0.1f; break;
            case HEAL: anim = "Heal1"; loops = 1; time = 0.2f; break;
            case EMOTE: anim = "Emote1"; loops = 1; time = 0.2f; break;
            case DYING: anim = "Die"; loops = 1; time = 0.3f; break;
        }
        if (animationController != null && !anim.isEmpty() && (!anim.equals(currentAnimName) || force || loops == 1)) {
            currentAnimName = anim;
            try {
                if (loops == 1) {
                    animationController.animate(anim, loops, 1f, new AnimationController.AnimationListener() {
                        @Override public void onEnd(AnimationController.AnimationDesc a) { actionFinished(); }
                        @Override public void onLoop(AnimationController.AnimationDesc a) {}
                    }, time);
                } else animationController.animate(anim, loops, 1f, null, time);
            } catch (Exception e) { actionFinished(); }
        }
    }

    private void actionFinished() {
        if (currentState == State.DYING) return;
        if (bufferedState != null && bufferTimer > 0) { State next = bufferedState; bufferedState = null; requestState(next); }
        else currentState = State.IDLE;
    }

    private float lerpAngle(float cur, float tgt, float spd) {
        float diff = tgt - cur;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return cur + diff * spd;
    }

    private void applyTransform() {
        if (playerScene != null) {
            boolean isFPS = (context.camera != null && context.camera.getCurrentMode() == AdvancedCameraSystem.CameraMode.FIRST_PERSON);
            float s = isFPS ? 0f : skalaKarakter;
            playerScene.modelInstance.transform.setToTranslation(position.x, position.y + visualYOffset, position.z).rotate(Vector3.Y, yaw).scale(s, s, s);
        }
    }

    public void dispose() {
        if (context.sceneRenderer != null && playerScene != null) {
            context.sceneRenderer.removeScene(playerScene);
            if (auraLight != null) context.sceneRenderer.getEnvironment().remove(auraLight);
        }
    }
}
