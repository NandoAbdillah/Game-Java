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
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;

public class KunangKunang extends Entity {

    private static Model sharedParticleModel;

    private Scene bugScene;
    private float timeOffset;
    private Vector3 originPoint;

    private float speedX, speedY, speedZ;

    public KunangKunang(Vector3 startPos, GameContext context) {
        super(startPos, context);
        this.originPoint = new Vector3(startPos);

        this.timeOffset = MathUtils.random(0f, 100f);
        this.speedX = MathUtils.random(0.5f, 1.5f);
        this.speedY = MathUtils.random(0.8f, 2.0f);
        this.speedZ = MathUtils.random(0.5f, 1.5f);

        buildParticle();
    }

    private void buildParticle() {
        if (sharedParticleModel == null) {
            ModelBuilder mb = new ModelBuilder();
            Material mat = new Material(
                ColorAttribute.createDiffuse(Color.YELLOW),
                ColorAttribute.createEmissive(Color.GOLD)
            );
            sharedParticleModel = mb.createBox(0.06f, 0.06f, 0.06f, mat, Usage.Position | Usage.Normal);
        }

        bugScene = new Scene(new ModelInstance(sharedParticleModel));
        context.sceneRenderer.addScene(bugScene);
    }

    @Override
    public void update(float delta) {
        timeOffset += delta;

        position.x = originPoint.x + MathUtils.sin(timeOffset * speedX) * 2.5f;
        position.y = originPoint.y + MathUtils.sin(timeOffset * speedY) * 1.5f;
        position.z = originPoint.z + MathUtils.cos(timeOffset * speedZ) * 2.5f;

        bugScene.modelInstance.transform.setToTranslation(position);
    }

    public void dispose() {
        if (context.sceneRenderer != null && bugScene != null) {
            context.sceneRenderer.removeScene(bugScene);
        }
    }
}
