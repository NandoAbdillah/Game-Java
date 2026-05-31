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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.input.InputAction;
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

    public float dangerTimer = 0f;
    private Color baseGlow = new Color(0.05f, 0.05f, 0.08f, 1f), healGlow = new Color(0.1f, 0.9f, 0.3f, 1f), currentGlow = new Color(baseGlow);
    private float healGlowTimer = 0f, footstepTimer = 0f;

    // --- FIX AAA: Cooldown 5 Detik! ---
    private float lastThrowTime = 0f;
    private static final float THROW_COOLDOWN = 5.0f;

    public Player(GameContext context, SceneAsset asset) {
        super(new Vector3(0, PLAYER_HEIGHT, 0), context);
        this.collisionRadius = 1.0f;
        this.collisionHeight = 4.0f;
        setupVisuals(asset);
    }

    private void setupVisuals(SceneAsset asset) {
        playerScene = new Scene(asset.scene);
        for (Material material : playerScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }
        applyTransform();
        animationController = playerScene.animationController;
        if (animationController != null) changeState(State.IDLE, true);
        if (context.sceneRenderer != null) context.sceneRenderer.addScene(playerScene);
    }

    public void addDanger(float amount) {
        dangerTimer += amount;
        if (dangerTimer >= 10f && currentState != State.DYING) {
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

        // FIX AAA: Pindahkan SEMUA timer ke sini agar DIJAMIN berjalan setiap frame!
        if (lastThrowTime > 0) lastThrowTime -= delta;
        if (dangerTimer > 0) dangerTimer -= delta * 0.5f;

        if (healGlowTimer > 0) {
            healGlowTimer -= delta;
            currentGlow.set(baseGlow).lerp(healGlow, MathUtils.clamp(healGlowTimer / 2.0f, 0f, 1f));
        } else {
            currentGlow.set(baseGlow);
        }

        for (Material mat : playerScene.modelInstance.materials) {
            ColorAttribute emissive = (ColorAttribute) mat.get(ColorAttribute.Emissive);
            if (emissive != null) emissive.color.set(currentGlow);
        }

        if (input.toggleCameraPressed) context.camera.toggleMode();
        if (input.diePressed) requestState(State.DYING);
        if (input.healPressed) requestState(State.HEAL);
        if (input.emotePressed) requestState(State.EMOTE);

        // LOGIKA PENGECEKAN COOLDOWN 5 DETIK
        if (input.throwPressed && lastThrowTime <= 0) {
            throwBiji();
            requestState(State.THROW);
            lastThrowTime = THROW_COOLDOWN; // Kunci tombol 'E' selama 5 detik!
        }

        if (input.kickPressed) requestState(State.KICK);
        if (input.attackPressed) requestState(State.COMBAT);
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

        // Lempar 15 peluru menyebar!
        for (int i = 0; i < 15; i++) {
            context.entityManager.addEntity(new BijiTimunProjectile(
                new Vector3(spawnX, position.y + 2.5f, spawnZ),
                yaw + MathUtils.random(-35f, 35f),
                context,
                (i == 0) // FIX: Hanya peluru pertama yang bunyi, biar HP ga ngelag/budek!
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
        if (context.sceneRenderer != null && playerScene != null) context.sceneRenderer.removeScene(playerScene);
    }
}
