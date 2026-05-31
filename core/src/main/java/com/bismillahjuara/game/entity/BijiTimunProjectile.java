package com.bismillahjuara.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;

public class BijiTimunProjectile extends Entity {

    private Scene bijiScene;
    private Vector3 forwardDir = new Vector3();
    private float speed;
    private float verticalVelocity;
    private float gravity = -25f;
    private float lifeTimer = 3.0f;

    // --- FIX AAA: Sinkronisasi Animasi Tangan Player ---
    private float delayTimer = 1.2f;
    private boolean canMove = false;
    private boolean isVisible = false; // Sembunyikan selama ancang-ancang!

    public BijiTimunProjectile(Vector3 startPos, float yaw, GameContext context, boolean b) {
        super(startPos, context);
        this.yaw = yaw;

        float yawRad = MathUtils.degreesToRadians * yaw;
        forwardDir.set(MathUtils.sin(yawRad), 0, MathUtils.cos(yawRad)).nor();

        // Speed dilebarkan jangkauannya agar hamburan peluru terasa lebih acak dan natural
        this.speed = MathUtils.random(12f, 25f);
        this.verticalVelocity = MathUtils.random(6f, 10f);

        buildSeed();
    }

    private void buildSeed() {
        ModelBuilder mb = new ModelBuilder();
        Material mat = new Material(
            ColorAttribute.createDiffuse(new Color(0.95f, 0.93f, 0.8f, 1f)),
            ColorAttribute.createEmissive(new Color(0.4f, 0.8f, 0.2f, 1f))
        );
        Model box = mb.createBox(0.04f, 0.02f, 0.08f, mat, Usage.Position | Usage.Normal);
        ModelInstance instance = new ModelInstance(box);
        bijiScene = new Scene(instance);

        // PENTING: Jangan render ke layar dulu (tunggu animasi tangan 1.2 detik selesai)
    }

    @Override
    public void update(float delta) {
        // 1. TUNGGU ANIMASI TANGAN SELESAI
        if (!canMove) {
            delayTimer -= delta;
            if (delayTimer <= 0) {
                canMove = true;
                isVisible = true;

                // Munculkan biji ke layar dan putar suara tembakan TEPAT saat delay habis
                context.sceneRenderer.addScene(bijiScene);
                context.audio.playSFX(AudioSFX.JIMAT_THROW);
            }
            return;
        }

        // 2. PELURU MELAYANG
        lifeTimer -= delta;
        if (lifeTimer <= 0) {
            destroy();
            return;
        }

        verticalVelocity += gravity * delta;
        position.x += forwardDir.x * speed * delta;
        position.y += verticalVelocity * delta;
        position.z += forwardDir.z * speed * delta;

        if (position.y <= 0.05f) {
            position.y = 0.05f;
            verticalVelocity = 0;
            speed = 0;
        }

        bijiScene.modelInstance.transform.setToTranslation(position);

        if (speed > 0) {
            bijiScene.modelInstance.transform.rotate(Vector3.X, MathUtils.random(500f, 1000f) * delta);
            bijiScene.modelInstance.transform.rotate(Vector3.Y, MathUtils.random(500f, 1000f) * delta);
        }

        checkCollisionWithEnemies();
    }

    private void checkCollisionWithEnemies() {
        if (speed <= 0) return;

        for (Entity e : context.entityManager.getEntities()) {
            if (e instanceof SukmaGowong) {
                SukmaGowong enemy = (SukmaGowong) e;
                if (!enemy.isDead() && enemy.getPosition().dst(this.position) < 2.5f) {
                    enemy.takeBurnDamage();
                    destroy();
                    return;
                }
            }
            // FIX PHASE 3: Deteksi Serangan ke Buto Ijo
            else if (e instanceof ButoIjo) {
                ButoIjo boss = (ButoIjo) e;
                // Hitbox boss lebih besar (3.5f)
                if (!boss.isDead() && boss.getPosition().dst(this.position) < 3.5f) {
                    boss.takeHit();
                    destroy();
                    return;
                }
            }
        }
    }

    private void destroy() {
        if (isVisible) {
            context.sceneRenderer.removeScene(bijiScene);
        }
        context.entityManager.removeEntity(this);
    }
}
