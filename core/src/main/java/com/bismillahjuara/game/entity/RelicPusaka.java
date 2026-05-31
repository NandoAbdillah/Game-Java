package com.bismillahjuara.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.core.GameContext;

import net.mgsx.gltf.scene3d.scene.Scene;

public class RelicPusaka extends Entity {

    private Scene scene;
    private float floatTimer = 0f;

    public RelicPusaka(Vector3 startPos, GameContext context) {
        super(startPos, context);

        // Buat objek emas bercahaya (Sementara sebelum ada model 3D)
        ModelBuilder mb = new ModelBuilder();
        Material mat = new Material(
            ColorAttribute.createDiffuse(Color.GOLD),
            ColorAttribute.createEmissive(new Color(0.6f, 0.4f, 0.1f, 1f))
        );
        Model box = mb.createBox(0.8f, 0.8f, 0.8f, mat, Usage.Position | Usage.Normal);
        scene = new Scene(new ModelInstance(box));

        context.sceneRenderer.addScene(scene);
    }

    @Override
    public void update(float delta) {
        // Animasi Muter & Melayang
        yaw += 90f * delta;
        floatTimer += delta * 2f;
        float floatY = position.y + 1.0f + ((float)Math.sin(floatTimer) * 0.3f);

        scene.modelInstance.transform.setToTranslation(position.x, floatY, position.z).rotate(Vector3.Y, yaw);

        // Deteksi Pungut oleh Timun Mas (Jarak 2 Meter)
        if (context.player != null && position.dst(context.player.getPosition()) < 2.0f) {
            context.relicsCollected++;
            context.audio.playSFX(AudioSFX.ENV_CHEST); // Suara pungut

            context.sceneRenderer.removeScene(scene);
            context.entityManager.removeEntity(this);
        }
    }
}
