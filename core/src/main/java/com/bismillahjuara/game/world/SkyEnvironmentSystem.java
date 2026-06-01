package com.bismillahjuara.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.audio.AudioManager;
import com.bismillahjuara.game.audio.AudioSFX;

import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * AAA Living Sky Environment.
 * Mengelola 360 Panorama, pergerakan awan/kabut, dan state machine Petir + Suara.
 */
public class SkyEnvironmentSystem {

    private ModelBatch skyBatch;
    private Model skyModel, cloudModel, fogModel;
    private ModelInstance skyInstance, cloudInstance, fogInstance;
    private Texture skyTex, cloudTex, fogTex;

    private SceneManager targetSceneManager;
    private DirectionalLightEx targetSunLight;

    // --- FIX AAA: SETTING HORROR SEJATI ---
    // Ambient: Sangat redup agar area tanpa senter terlihat hitam (0.05f)
    private final Color normalAmbientColor = new Color(0.05f, 0.05f, 0.08f, 1f);
    // Matahari/Bulan: Warna biru malam pucat, tidak menyilaukan
    private final Color normalSunColor = new Color(0.2f, 0.3f, 0.4f, 1f);
    private final float normalSunIntensity = 0.2f;

    private final Color flashColor = new Color(0.8f, 0.9f, 1.0f, 1f);

    private float nextStrikeTimer = 5f;
    private boolean isFlashing = false;
    private float flashDuration = 0f;
    private float flashIntensityTarget = 0f;

    private boolean isWaitingForThunder = false;
    private float thunderDelayTimer = 0f;

    public SkyEnvironmentSystem(SceneManager sceneManager, DirectionalLightEx sunLight) {
        this.skyBatch = new ModelBatch();
        this.targetSceneManager = sceneManager;
        this.targetSunLight = sunLight;

        loadTextures();
        buildNestedSpheres();
        resetLightingToNormal();
    }

    private void loadTextures() {
        // FIX AAA: Kalau panorama.png ga ada, warnanya jadi Biru Malam Estetik (Bukan hitam buta)
        skyTex = loadTextureSafe("textures/panorama.png", new Color(0.15f, 0.25f, 0.45f, 1f));
        cloudTex = loadTextureSafe("textures/clouds.png", new Color(1f, 1f, 1f, 0.0f));
        fogTex = loadTextureSafe("textures/fog.png", new Color(0.5f, 0.5f, 0.5f, 0.0f));
    }

    private Texture loadTextureSafe(String path, Color fallback) {
        if (Gdx.files.internal(path).exists()) {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
            return t;
        } else {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(fallback);
            pixmap.fill();
            Texture t = new Texture(pixmap);
            pixmap.dispose();
            return t;
        }
    }

    private void buildNestedSpheres() {
        ModelBuilder mb = new ModelBuilder();
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates;
        int divisions = 32;

        Material skyMat = new Material(
            TextureAttribute.createDiffuse(skyTex),
            IntAttribute.createCullFace(0),
            new DepthTestAttribute(false)
        );
        skyModel = mb.createSphere(200f, 200f, 200f, divisions, divisions, skyMat, attr);
        skyInstance = new ModelInstance(skyModel);

        Material cloudMat = new Material(
            TextureAttribute.createDiffuse(cloudTex),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
            IntAttribute.createCullFace(0),
            new DepthTestAttribute(false)
        );
        cloudModel = mb.createSphere(190f, 190f, 190f, divisions, divisions, cloudMat, attr);
        cloudInstance = new ModelInstance(cloudModel);

        Material fogMat = new Material(
            TextureAttribute.createDiffuse(fogTex),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
            IntAttribute.createCullFace(0),
            new DepthTestAttribute(false)
        );
        fogModel = mb.createSphere(180f, 180f, 180f, divisions, divisions, fogMat, attr);
        fogInstance = new ModelInstance(fogModel);
    }

    public void update(float delta, Vector3 cameraPosition) {
        float skyYOffset = cameraPosition.y - 15f;

        skyInstance.transform.setToTranslation(cameraPosition.x, skyYOffset, cameraPosition.z);
        cloudInstance.transform.setToTranslation(cameraPosition.x, skyYOffset, cameraPosition.z)
            .rotate(Vector3.Y, Gdx.graphics.getFrameId() * 0.02f);
        fogInstance.transform.setToTranslation(cameraPosition.x, skyYOffset, cameraPosition.z)
            .rotate(Vector3.Y, Gdx.graphics.getFrameId() * 0.05f);

        handleLightningAndThunder(delta);
    }

    private void handleLightningAndThunder(float delta) {
        if (!isFlashing) {
            nextStrikeTimer -= delta;
            if (nextStrikeTimer <= 0) {
                triggerLightningStrike();
            }
        } else {
            flashDuration -= delta;
            if (flashDuration > 0) {
                float flicker = MathUtils.random(0.5f, 1.0f);
                targetSunLight.color.set(flashColor);
                targetSunLight.intensity = flashIntensityTarget * flicker;
                targetSceneManager.setAmbientLight(0.8f * flicker);
            } else {
                resetLightingToNormal();
                isFlashing = false;
                nextStrikeTimer = MathUtils.random(10f, 30f);
            }
        }

        if (isWaitingForThunder) {
            thunderDelayTimer -= delta;
            if (thunderDelayTimer <= 0) {
                playRandomThunderSound();
                isWaitingForThunder = false;
            }
        }
    }

    private void triggerLightningStrike() {
        isFlashing = true;
        flashDuration = MathUtils.random(0.1f, 0.4f);
        flashIntensityTarget = MathUtils.random(3.0f, 5.0f);

        isWaitingForThunder = true;
        thunderDelayTimer = MathUtils.random(1.0f, 4.0f);
    }

    private void playRandomThunderSound() {
        AudioSFX[] thunders = { AudioSFX.ENV_THUNDER_1, AudioSFX.ENV_THUNDER_2 };
        AudioSFX selected = thunders[MathUtils.random(0, thunders.length - 1)];
        AudioManager.getInstance().playSFX(selected);
    }

    private void resetLightingToNormal() {
        targetSunLight.color.set(normalSunColor);
        targetSunLight.intensity = normalSunIntensity;
        targetSceneManager.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, normalAmbientColor));
    }

    public void render(PerspectiveCamera camera) {
        Gdx.gl.glDepthMask(false);
        skyBatch.begin(camera);
        skyBatch.render(skyInstance);
        skyBatch.render(cloudInstance);
        skyBatch.render(fogInstance);
        skyBatch.end();
        Gdx.gl.glDepthMask(true);
    }

    public void dispose() {
        skyBatch.dispose();
        skyModel.dispose();
        cloudModel.dispose();
        fogModel.dispose();
        skyTex.dispose();
        cloudTex.dispose();
        fogTex.dispose();
    }
}
