package com.bismillahjuara.game.entity;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.bismillahjuara.game.core.GameContext;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class EnvironmentProp extends Entity {

    private Scene propScene;
    private Vector3 rot;
    private Vector3 scale;

    public EnvironmentProp(Vector3 pos, Vector3 rotDegrees, Vector3 scale, GameContext context, SceneAsset asset) {
        super(pos, context);
        this.rot = new Vector3(rotDegrees);
        this.scale = new Vector3(scale);
        this.yaw = rotDegrees.y;

        if (asset != null) {
            propScene = new Scene(asset.scene);

            // Optimasi Material
            for (Material material : propScene.modelInstance.materials) {
                material.remove(BlendingAttribute.Type);
                material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
                material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            }

            // Terapkan skala dan posisi sebelum mengukur BoundingBox!
            propScene.modelInstance.transform.setToTranslation(pos)
                .rotate(Vector3.Y, rot.y)
                .rotate(Vector3.X, rot.x)
                .rotate(Vector3.Z, rot.z)
                .scale(scale.x, scale.y, scale.z);

            // --- FIX AAA: MENGUKUR DAN MENDAFTARKAN COLLISION ANTI TEMBUS ---
            BoundingBox box = new BoundingBox();
            propScene.modelInstance.calculateBoundingBox(box);
            // Kalikan dengan skala dan rotasinya agar presisi 100%
            box.mul(propScene.modelInstance.transform);

            // Setor tembok ini ke WorldManager!
            context.worldManager.addCustomCollision(box);

            context.sceneRenderer.addScene(propScene);
        }
    }

    @Override
    public void update(float delta) {
        if (propScene != null) {
            propScene.modelInstance.transform.setToTranslation(position)
                .rotate(Vector3.Y, rot.y)
                .rotate(Vector3.X, rot.x)
                .rotate(Vector3.Z, rot.z)
                .scale(scale.x, scale.y, scale.z);
        }
    }

    public void dispose() {
        if (context.sceneRenderer != null && propScene != null) {
            context.sceneRenderer.removeScene(propScene);
        }
    }
}
