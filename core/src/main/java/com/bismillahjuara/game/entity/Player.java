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

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Player extends Entity {

    public enum State {
        IDLE(0), WALK(0), RUN(0), CROUCH_IDLE(0), CROUCH_WALK(0), COMBAT_IDLE(0),
        JUMP(1), JUMP_RUN(1),
        THROW(2), KICK(2), COMBAT(2), EMOTE(2), HEAL(2),
        DYING(99);
        public final int priority;
        State(int priority) { this.priority = priority; }
    }

    private static final float SPEED_WALK = 6f;
    private static final float SPEED_RUN = 14f;
    private static final float SPEED_CROUCH = 3f;
    private static final float ACCELERATION = 10f;
    private static final float DECELERATION = 15f;
    private static final float ROTATION_SMOOTH = 15f;
    private static final float GRAVITY = -35f;
    private static final float JUMP_POWER = 14f;
    private static final float PLAYER_HEIGHT = 0f;

    private Vector2 currentVelocity2D = new Vector2();
    private float verticalVelocity = 0f;

    private State currentState = State.IDLE;
    private String currentAnimName = "";

    private boolean isCrouching = false;
    private boolean isCombatMode = false;
    private float combatModeTimer = 0f;
    private static final float COMBAT_TIMEOUT = 5f;

    private State bufferedState = null;
    private float bufferTimer = 0f;
    private static final float BUFFER_WINDOW = 0.3f;

    private SceneAsset sceneAsset;
    private Scene playerScene;
    private AnimationController animationController;
    private float skalaKarakter = 4.0f;


    // --- FIX TENGGELAM (VISUAL OFFSET) ---
    // Sesuaikan angka ini (misal 2.0f atau 3.5f) sampai telapak kaki pas di tanah.
    // Ini HANYA mengangkat wujudnya, fisika kakinya tetap kokoh di Y=0!
    private float visualYOffset = 3.0f;

    // --- HORROR SURVIVAL STATE ---
    public float dangerTimer = 0f;
    private final float MAX_DANGER_TIME = 10f;

    // --- VISUAL GLOW SYSTEM (ANDROID FRIENDLY) ---
    private Color baseGlow = new Color(0.05f, 0.05f, 0.08f, 1f); // Glow tipis visibilitas
    private Color healGlow = new Color(0.1f, 0.9f, 0.3f, 1f);    // Glow terang saat nge-heal
    private Color currentGlow = new Color(baseGlow);
    private float healGlowTimer = 0f;

    public Player(GameContext context) {
        super(new Vector3(0, PLAYER_HEIGHT, 0), context);
        setupGLTF();
    }


        public void addDanger(float amount) {
            dangerTimer += amount;
            if (dangerTimer >= MAX_DANGER_TIME) {
                triggerDeath();
            }
        }

        private void triggerDeath() {
            if (currentState != State.DYING) {
                changeState(State.DYING, true);
                // Memberitahu sistem bahwa game over
                context.state = com.bismillahjuara.game.core.GameplayState.GAME_OVER;
            }
        }

    private void setupGLTF() {
        sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/TimunAnim2.glb"));
        playerScene = new Scene(sceneAsset.scene);

        for (Material material : playerScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        applyTransform();

        animationController = playerScene.animationController;
        if (animationController != null) {
            changeState(State.IDLE, true);
        }

        if (context.sceneRenderer != null) {
            context.sceneRenderer.addScene(playerScene);
        } else {
            Gdx.app.error("PLAYER", "CRITICAL ERROR: SceneRenderer null di Context!");
        }
    }

    @Override
    public void update(float delta) {
        // Logika Horror: Danger level menurun perlahan jika berhasil kabur
        if (dangerTimer > 0) {
            dangerTimer -= delta * 0.5f;
        }

        // --- UPDATE GLOW EFFECT (Interpolasi Mulus) ---
        if (healGlowTimer > 0) {
            healGlowTimer -= delta;
            float progress = MathUtils.clamp(healGlowTimer / 2.0f, 0f, 1f); // 2 Detik efek heal
            currentGlow.set(baseGlow).lerp(healGlow, progress);
        } else {
            currentGlow.set(baseGlow);
        }

        // Terapkan warna glow ke material GLTF
        if (playerScene != null) {
            for (Material material : playerScene.modelInstance.materials) {
                ColorAttribute emissive = (ColorAttribute) material.get(ColorAttribute.Emissive);
                if (emissive != null) emissive.color.set(currentGlow);
            }
        }
    }

    public void processInputAndPhysics(InputAction input, float camYaw, float delta) {

        if (currentState == State.DYING) return;

        if (input.toggleCameraPressed) {
            context.camera.toggleMode();
        }

        if (input.diePressed) requestState(State.DYING);
        if (input.healPressed) requestState(State.HEAL);
        if (input.emotePressed) requestState(State.EMOTE);
        if (input.throwPressed) requestState(State.THROW);
        if (input.kickPressed) requestState(State.KICK);
        if (input.attackPressed) requestState(State.COMBAT);

        if (input.crouchToggled) {
            if (currentState.priority == 0) {
                isCrouching = !isCrouching;
                isCombatMode = false;
            }
        }

        if (input.jumpPressed) {
            if (input.sprintHeld && (input.moveX != 0 || input.moveY != 0)) {
                requestState(State.JUMP_RUN);
            } else {
                requestState(State.JUMP);
            }
        }

        if (bufferTimer > 0) bufferTimer -= delta;
        else bufferedState = null;

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
            if (currentState == State.JUMP || currentState == State.JUMP_RUN) {
                actionFinished();
            }
        }

        if (input.throwPressed) {
            throwBiji();
            requestState(State.THROW);
        }



        // =========================================================
        // FIX BUG: PASANG DEADZONE AGAR ANIMASI IDLE/WALK TIDAK FLICKER
        // Joystick harus digeser lebih dari 0.1 agar dianggap bergerak
        // =========================================================
        boolean hasInput = (Math.abs(input.moveX) > 0.1f || Math.abs(input.moveY) > 0.1f);
        Vector2 targetVelocity2D = new Vector2();

        if (currentState.priority == 0 || !isGrounded) {
            if (hasInput) {
                float targetSpeed = SPEED_WALK;
                State locState = State.WALK;

                boolean crouchMode = isCrouching || input.crouchHeld;

                if (crouchMode) {
                    targetSpeed = SPEED_CROUCH;
                    locState = State.CROUCH_WALK;
                } else if (input.sprintHeld && !isCombatMode) {
                    targetSpeed = SPEED_RUN;
                    locState = State.RUN;
                }

                float yawRad = MathUtils.degreesToRadians * camYaw;
                float forwardX = -MathUtils.sin(yawRad);
                float forwardZ = -MathUtils.cos(yawRad);
                float rightX   =  MathUtils.cos(yawRad);
                float rightZ   = -MathUtils.sin(yawRad);

                targetVelocity2D.x = (forwardX * input.moveY + rightX * input.moveX) * targetSpeed;
                targetVelocity2D.y = (forwardZ * input.moveY + rightZ * input.moveX) * targetSpeed;

                if (isGrounded) changeState(locState, false);

                float targetRotation = MathUtils.atan2(targetVelocity2D.x, targetVelocity2D.y) * MathUtils.radiansToDegrees;
                yaw = lerpAngle(yaw, targetRotation, ROTATION_SMOOTH * delta);

            } else {
                targetVelocity2D.setZero();
                if (isGrounded) {
                    changeState((isCrouching || input.crouchHeld) ? State.CROUCH_IDLE : State.IDLE, false);
                }
            }
        } else {
            targetVelocity2D.setZero();
        }

        float accelRate = hasInput ? ACCELERATION : DECELERATION;
        if (!isGrounded) accelRate *= 0.2f;

        currentVelocity2D.lerp(targetVelocity2D, accelRate * delta);

        float stepX = currentVelocity2D.x * delta;
        float stepZ = currentVelocity2D.y * delta;

        float playerRadius = 1.0f;
        float playerHeight = 4.0f;

        Vector3 nextPosX = new Vector3(position.x + stepX, position.y, position.z);
        if (context != null && context.worldManager != null && !context.worldManager.isColliding(nextPosX, playerRadius, playerHeight)) {
            position.x += stepX;
        }

        Vector3 nextPosZ = new Vector3(position.x, position.y, position.z + stepZ);
        if (context != null && context.worldManager != null && !context.worldManager.isColliding(nextPosZ, playerRadius, playerHeight)) {
            position.z += stepZ;
        }

        applyTransform();
    }

    private void throwBiji() {
        // AAA PROJECTILE FIX: Spawn Biji Timun di DEPAN player, bukan di dalam perut!
        float yawRad = MathUtils.degreesToRadians * yaw;
        // Wajib minus (-) untuk arah maju di LibGDX
        float forwardX = -MathUtils.sin(yawRad);
        float forwardZ = -MathUtils.cos(yawRad);

        // Titik awal spawn (Maju 1.5 meter, Naik 2.5 meter ke tangan)
        float spawnX = position.x + (forwardX * 1.5f);
        float spawnY = position.y + 2.5f;
        float spawnZ = position.z + (forwardZ * 1.5f);

        for (int i = 0; i < 6; i++) {
            float spreadYaw = yaw + MathUtils.random(-15f, 15f);

            BijiTimunProjectile biji = new BijiTimunProjectile(
                new Vector3(spawnX, spawnY, spawnZ),
                spreadYaw,
                context
            );
            context.entityManager.addEntity(biji);
        }
    }

    private void requestState(State requestedState) {
        if (currentState == State.DYING) return;

        if (requestedState.priority >= currentState.priority) {
            if (requestedState == State.JUMP || requestedState == State.JUMP_RUN) {
                if (position.y > PLAYER_HEIGHT + 0.1f) return;
                verticalVelocity = JUMP_POWER;
                isCrouching = false;
            }
            if (requestedState == State.COMBAT || requestedState == State.KICK) {
                isCombatMode = true;
                combatModeTimer = COMBAT_TIMEOUT;
                isCrouching = false;
            }
            changeState(requestedState, false);
        } else {
            bufferedState = requestedState;
            bufferTimer = BUFFER_WINDOW;
        }
    }

    private void changeState(State newState, boolean force) {
        if (!force && currentState == newState && newState.priority < 2) return;

        this.currentState = newState;
        String animName = "";
        float transitionTime = 0.15f;
        int loops = -1;

        switch (newState) {
            case IDLE:        animName = isCombatMode ? "CombatIdle" : "Idle"; break;
            case WALK:        animName = isCombatMode ? "SneakWalk" : "Walk"; break;
            case RUN:         animName = "Run"; break;
            case CROUCH_IDLE: animName = "CrouchIdle"; break;
            case CROUCH_WALK: animName = "CrouchWalk"; break;
            case JUMP:        animName = "Jump"; loops = 1; transitionTime = 0.1f; break;
            case JUMP_RUN:    animName = "JumpRun"; loops = 1; transitionTime = 0.1f; break;
            case COMBAT:      animName = "Combat"; loops = 1; transitionTime = 0.05f; break;
            case KICK:        animName = "Kick"; loops = 1; transitionTime = 0.05f; break;
            case THROW:       animName = "Throw"; loops = 1; transitionTime = 0.1f; break;
            case HEAL:        animName = "Heal1"; loops = 1; transitionTime = 0.2f; break;
            case EMOTE:       animName = "Emote1"; loops = 1; transitionTime = 0.2f; break;
            case DYING:       animName = "Die"; loops = 1; transitionTime = 0.3f; break;
        }

        if (animationController != null && !animName.isEmpty() && (!animName.equals(currentAnimName) || force || loops == 1)) {
            currentAnimName = animName;
            try {
                if (loops == 1) {
                    animationController.animate(animName, loops, 1f, new AnimationController.AnimationListener() {
                        @Override public void onEnd(AnimationController.AnimationDesc animation) { actionFinished(); }
                        @Override public void onLoop(AnimationController.AnimationDesc animation) {}
                    }, transitionTime);
                } else {
                    animationController.animate(animName, loops, 1f, null, transitionTime);
                }
            } catch (Exception e) {
                actionFinished();
            }
        }
    }

    private void actionFinished() {
        if (currentState == State.DYING) return;
        if (bufferedState != null && bufferTimer > 0) {
            State nextState = bufferedState;
            bufferedState = null;
            requestState(nextState);
        } else {
            currentState = State.IDLE;
        }
    }

    private float lerpAngle(float current, float target, float speed) {
        float diff = target - current;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return current + diff * speed;
    }

    private void applyTransform() {
        if (playerScene != null) {
            boolean isFPS = (context.camera != null && context.camera.getCurrentMode() == AdvancedCameraSystem.CameraMode.FIRST_PERSON);
            float renderScale = isFPS ? 0.0f : skalaKarakter;

            playerScene.modelInstance.transform
                .setToTranslation(position)
                .rotate(Vector3.Y, yaw)
                .scale(renderScale, renderScale, renderScale);
        }
    }

    public void dispose() {
        if (context.sceneRenderer != null && playerScene != null) {
            context.sceneRenderer.removeScene(playerScene);
        }
        if (sceneAsset != null) sceneAsset.dispose();
    }
}
