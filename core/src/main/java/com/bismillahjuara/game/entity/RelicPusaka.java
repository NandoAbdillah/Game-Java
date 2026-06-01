package com.bismillahjuara.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class RelicPusaka extends Entity {

    public enum RelicType {
        KERIS, KUJANG, MANDAU
    }

    private Scene scene;
    private float floatTimer = 0f;
    private Vector3 basePos;

    public RelicType type;
    private Color baseGlowColor;
    private Color currentGlowColor = new Color();

    // --- SKALA MODEL GLB (UBAH ANGKA INI JIKA TERLALU KECIL/BESAR) ---
    public float scale = 2.5f;

    public RelicPusaka(Vector3 startPos, GameContext context, SceneAsset asset, RelicType type) {
        super(startPos, context);
        this.basePos = new Vector3(startPos);
        this.type = type;

        // Tetapkan warna mistis berdasarkan tipe senjata
        switch (type) {
            case KERIS:  baseGlowColor = new Color(1.0f, 0.8f, 0.1f, 1f); break; // Kuning Emas
            case KUJANG: baseGlowColor = new Color(0.2f, 1.0f, 0.4f, 1f); break; // Hijau Mistis
            case MANDAU: baseGlowColor = new Color(0.2f, 0.6f, 1.0f, 1f); break; // Biru Kebiruan
        }

        if (asset != null) {
            scene = new Scene(asset.scene);

            for (Material material : scene.modelInstance.materials) {
                material.remove(BlendingAttribute.Type);
                material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
                material.set(IntAttribute.createCullFace(GL20.GL_BACK));

                // Pastikan material punya atribut Emissive untuk di-glow
                if (!material.has(ColorAttribute.Emissive)) {
                    material.set(ColorAttribute.createEmissive(Color.BLACK));
                }
            }
            context.sceneRenderer.addScene(scene);
        }
    }

    @Override
    public void update(float delta) {
        if (scene == null) return;

        // 1. ANIMASI FLOATING (Naik Turun) & ROTATING
        yaw += 60f * delta;
        floatTimer += delta;
        float floatY = basePos.y + 1.0f + (MathUtils.sin(floatTimer * 2f) * 0.2f);

        scene.modelInstance.transform.setToTranslation(position.x, floatY, position.z)
            .rotate(Vector3.Y, yaw)
            .scale(scale, scale, scale);

        // 2. ANIMASI BLINKING & PROXIMITY GLOW
        float distToPlayer = position.dst(context.player.getPosition());

        // Blink statis: naik turun pelan (0.5 hingga 1.0)
        float glowIntensity = 0.5f + (Math.abs(MathUtils.sin(floatTimer * 3f)) * 0.5f);

        // Proximity: Jika jarak < 10m, semakin dekat semakin bersinar!
        if (distToPlayer < 10f) {
            float proximityFactor = 1.0f - (distToPlayer / 10f); // 0 (jauh) -> 1 (nempel)
            glowIntensity += (proximityFactor * 2.0f); // Tambah intensitas hingga 2x lipat
        }

        // Terapkan intensitas ke warna
        currentGlowColor.set(baseGlowColor).mul(glowIntensity);
        currentGlowColor.a = 1f; // Jaga alpha tetap utuh

        for (Material mat : scene.modelInstance.materials) {
            ColorAttribute emissive = (ColorAttribute) mat.get(ColorAttribute.Emissive);
            if (emissive != null) {
                emissive.color.set(currentGlowColor);
            }
        }

        // 3. LOGIKA COLLECT (Jarak < 2 Meter)
        if (distToPlayer < 2.0f) {
            context.relicsCollected++;
            context.audio.playSFX(AudioSFX.ENV_CHEST);

            context.sceneRenderer.removeScene(scene);
            context.entityManager.removeEntity(this);
        }
    }

    public void dispose() {
        if (context.sceneRenderer != null && scene != null) {
            context.sceneRenderer.removeScene(scene);
        }
    }
}
