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

    // --- FIX RENDER ARCHITECTURE ---
    private GameContext context;
    private SceneAsset sceneAsset;
    private Scene playerScene;
    private AnimationController animationController;
    private float skalaKarakter = 4.0f;

    // Constructor sekarang wajib minta GameContext agar terhubung dengan dunia
    public Player(GameContext context) {
        super(new Vector3(0, PLAYER_HEIGHT, 0));
        this.context = context;
        setupGLTF();
    }

    private void setupGLTF() {
        // KITA HAPUS INTERNAL SCENEMANAGER.
        // Rendering sekarang murni tugasnya RenderPipeline!
        sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/Timun.glb"));
        playerScene = new Scene(sceneAsset.scene);

        for (Material material : playerScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        applyTransform();
        animationController = new AnimationController(playerScene.modelInstance);
        changeState(State.IDLE, true);

        // INILAH KUNCINYA: Daftarkan model player ini ke SceneRenderer Global milik Context!
        if (context.sceneRenderer != null) {
            context.sceneRenderer.addScene(playerScene);
        } else {
            Gdx.app.error("PLAYER", "CRITICAL ERROR: SceneRenderer null di Context!");
        }
    }

    @Override
    public void update(float delta) {}

    public void processInputAndPhysics(InputAction input, float camYaw, float delta) {
        if (currentState == State.DYING) return;

        // --- SISTEM TOGGLE KAMERA V (Transisi FPS / TPS) ---
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

        boolean hasInput = (input.moveX != 0 || input.moveY != 0);
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

        position.x += currentVelocity2D.x * delta;
        position.z += currentVelocity2D.y * delta;

        float mapLimit = 48f;
        position.x = MathUtils.clamp(position.x, -mapLimit, mapLimit);
        position.z = MathUtils.clamp(position.z, -mapLimit, mapLimit);

        applyTransform();
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

        if (!animName.isEmpty() && (!animName.equals(currentAnimName) || force || loops == 1)) {
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
            // FIX Kamera FPS: Saat FPS, model disembunyikan agar kita tidak melihat bagian dalam tengkorak
            boolean isFPS = (context.camera != null && context.camera.getCurrentMode() == AdvancedCameraSystem.CameraMode.FIRST_PERSON);
            float renderScale = isFPS ? 0.0f : skalaKarakter;

            playerScene.modelInstance.transform
                .setToTranslation(position)
                .rotate(Vector3.Y, yaw)
                .scale(renderScale, renderScale, renderScale);
        }
        // PENTING: Update animasi wajib ditaruh di sini agar animasinya berdenyut!
        if (animationController != null) animationController.update(Gdx.graphics.getDeltaTime());
    }

    // Fungsi Render pribadi dihilangkan (KINI TUGAS SCENE RENDERER)

    public void dispose() {
        if (context.sceneRenderer != null && playerScene != null) {
            context.sceneRenderer.removeScene(playerScene); // Hapus bersih saat memory dealokasi
        }
        if (sceneAsset != null) sceneAsset.dispose();
    }
}
