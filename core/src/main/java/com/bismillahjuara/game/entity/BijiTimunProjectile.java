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

/**
 * Entitas Biji Timun Mistis.
 * Memiliki fisika lemparan menyebar, arah maju akurat, dan bentuk biji natural.
 */
public class BijiTimunProjectile extends Entity {

    private Scene bijiScene;
    private Vector3 forwardDir = new Vector3();

    // FISIKA LEMPARAN AAA
    private float speed;
    private float verticalVelocity;
    private float gravity = -25f;
    private float lifeTimer = 3.0f;

    public BijiTimunProjectile(Vector3 startPos, float yaw, GameContext context) {
        super(startPos, context);
        this.yaw = yaw;

        // FIX MUTLAK ARAH LEMPAR: Wajib menggunakan minus (-sin dan -cos) agar melesat ke DEPAN player
        float yawRad = MathUtils.degreesToRadians * yaw;
        forwardDir.set(-MathUtils.sin(yawRad), 0, -MathUtils.cos(yawRad)).nor();

        this.speed = MathUtils.random(15f, 22f);
        this.verticalVelocity = MathUtils.random(5f, 9f);

        buildGlowingSeed();

        context.audio.playSFX(AudioSFX.JIMAT_THROW);
    }

    private void buildGlowingSeed() {
        ModelBuilder mb = new ModelBuilder();

        // Material Biji Timun (Kecil, Putih Pucat, Glow Hijau)
        Material mat = new Material(
            ColorAttribute.createDiffuse(new Color(0.95f, 0.93f, 0.8f, 1f)), // Warna biji asli
            ColorAttribute.createEmissive(new Color(0.4f, 0.8f, 0.2f, 1f)) // Bersinar hijau mistis tipis
        );

        // 3. FIX BENTUK: Kecil seperti biji, tidak lagi seperti balok emas raksasa
        Model box = mb.createBox(0.04f, 0.02f, 0.08f, mat, Usage.Position | Usage.Normal);
        ModelInstance instance = new ModelInstance(box);
        bijiScene = new Scene(instance);

        context.sceneRenderer.addScene(bijiScene);
    }

    @Override
    public void update(float delta) {
        lifeTimer -= delta;
        if (lifeTimer <= 0) {
            destroy();
            return;
        }

        // FISIKA PARABOLA
        verticalVelocity += gravity * delta;
        position.x += forwardDir.x * speed * delta;
        position.y += verticalVelocity * delta;
        position.z += forwardDir.z * speed * delta;

        // MANTUL DI TANAH
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
                    destroy(); // Biji hancur setelah mengenai musuh
                    return;
                }
            }
        }
    }

    private void destroy() {
        context.sceneRenderer.removeScene(bijiScene);
        context.entityManager.removeEntity(this);
    }
}
