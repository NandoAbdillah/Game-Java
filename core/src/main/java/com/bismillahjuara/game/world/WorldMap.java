package com.bismillahjuara.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

// --- IMPORT UNTUK GLTF MAP & ENTITY ---
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

// HAPUS IMPORT INI
// import com.bismillahjuara.game.entity.SukmaGowong;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    // --- LAMA (Untuk objek dummy) ---
    private ModelBatch modelBatch;
    private Environment environment;
    private Array<ModelInstance> worldInstances = new Array<>();
    private List<Model> worldModels = new ArrayList<>();

    // --- BARU (Untuk Map.glb) ---
    private SceneManager sceneManager;
    private SceneAsset mapAsset;
    private Scene mapScene;

    // HAPUS ARRAY MUSUH INI KARENA SUDAH DIURUS ENTITY MANAGER
    // private Array<SukmaGowong> enemies = new Array<>();

    public WorldMap() {
        // 1. SETUP MODEL BATCH (Untuk box/pohon dummy lama)
        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.50f, 1f));
        environment.add(new DirectionalLight().set(1.0f, 0.95f, 0.85f, -1f, -1f, -0.4f));
        environment.add(new DirectionalLight().set(0.2f, 0.22f, 0.25f,  0.5f, 0.5f, 0.2f));

        buildWorld(); // Logika dummy lama tetap dieksekusi!

        // HAPUS PEMANGGILAN SPAWN MUSUH
        // spawnEnemies();
    }

    // HAPUS METHOD spawnEnemies() DAN updateEnemies() KESELURUHAN
    // private void spawnEnemies() { ... }
    // public void updateEnemies(float delta, Vector3 playerPosition) { ... }

    private void setupGLTFMap() {
        // Konfigurasi PBR Shader anti-crash
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numBones = 60; // Berjaga-jaga jika map punya animasi (misal: kincir angin)

        DepthShader.Config depthConfig = PBRShaderProvider.createDefaultDepthConfig();
        depthConfig.numBones = 60;

        sceneManager = new SceneManager(
            new PBRShaderProvider(config),
            new PBRDepthShaderProvider(depthConfig)
        );

        // Pencahayaan untuk Map GLTF
        sceneManager.setAmbientLight(0.45f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.5f;
        sceneManager.environment.add(sunLight);

        // LOAD FILE Map.glb
        // PASTIKAN NAMA DAN FOLDERNYA BENAR! (Misal: ada di android/assets/models/Map.glb)
        mapAsset = new GLBLoader().load(Gdx.files.internal("models/maps/Map.glb"));
        mapScene = new Scene(mapAsset.scene);

        sceneManager.addScene(mapScene);
    }

    private void buildWorld() {
        ModelBuilder mb = new ModelBuilder();

        // --- GROUND TILES ---
        int tilesX = 20, tilesZ = 20;
        float tileSize = 5f;
        Color[] grassColors = {
            new Color(0.38f, 0.62f, 0.25f, 1f),
            new Color(0.42f, 0.66f, 0.28f, 1f),
            new Color(0.35f, 0.58f, 0.23f, 1f),
            new Color(0.44f, 0.64f, 0.22f, 1f),
        };
        for (int x = -tilesX/2; x < tilesX/2; x++) {
            for (int z = -tilesZ/2; z < tilesZ/2; z++) {
                Color c = grassColors[(Math.abs(x * 3 + z * 7)) % grassColors.length];
                Model tile = mb.createBox(tileSize, 0.2f, tileSize,
                    new Material(ColorAttribute.createDiffuse(c)),
                    Usage.Position | Usage.Normal);
                worldModels.add(tile);
                ModelInstance inst = new ModelInstance(tile);
                inst.transform.setToTranslation(x * tileSize + tileSize/2f, -0.1f, z * tileSize + tileSize/2f);
                worldInstances.add(inst);
            }
        }

        // --- BORDER WALLS (batu di tepi) ---
        float mapHalf = tilesX / 2f * tileSize;
        Color stoneColor = new Color(0.5f, 0.48f, 0.45f, 1f);
        addBorderWall(mb, stoneColor, -mapHalf, 0, 0,          0.5f, 3f, mapHalf * 2f); // kiri
        addBorderWall(mb, stoneColor,  mapHalf, 0, 0,          0.5f, 3f, mapHalf * 2f); // kanan
        addBorderWall(mb, stoneColor, 0, 0, -mapHalf,          mapHalf * 2f, 3f, 0.5f); // atas
        addBorderWall(mb, stoneColor, 0, 0,  mapHalf,          mapHalf * 2f, 3f, 0.5f); // bawah

        // --- OBSTACLE BOXES (batu-batuan / peti) ---
        int[][] obstaclePositions = {
            { 3, 3}, {-5, 7}, {8, -4}, {-8, -6}, {12, 2},
            {-12, 5}, {5, -10}, {-3, -12}, {10, 10}, {-10, -10},
            {6, 8}, {-7, -3}, {15, -5}, {-15, 8}
        };
        Color[] boxColors = {
            new Color(0.65f, 0.50f, 0.35f, 1f), // kayu coklat
            new Color(0.55f, 0.55f, 0.55f, 1f), // batu abu
            new Color(0.70f, 0.60f, 0.40f, 1f), // pasir
        };
        for (int[] pos : obstaclePositions) {
            float sz = 1.5f + MathUtils.random(0.5f, 1.5f);
            float h  = 1f + MathUtils.random(0.5f, 2f);
            Color bc = boxColors[MathUtils.random(boxColors.length - 1)];
            Model box = mb.createBox(sz, h, sz,
                new Material(ColorAttribute.createDiffuse(bc)),
                Usage.Position | Usage.Normal);
            worldModels.add(box);
            ModelInstance bi = new ModelInstance(box);
            bi.transform.setToTranslation(pos[0] * tileSize/5f * 3f, h/2f, pos[1] * tileSize/5f * 3f);
            worldInstances.add(bi);
        }

        // --- POHON PLACEHOLDER (silinder batang + sphere daun) ---
        int[][] treePositions = {
            {4, -3}, {-6, 4}, {9, 7}, {-9, -7}, {2, 12},
            {-2, -13}, {13, -8}, {-13, 9}
        };
        for (int[] pos : treePositions) {
            float tx = pos[0] * tileSize / 5f * 3.5f;
            float tz = pos[1] * tileSize / 5f * 3.5f;

            // Batang
            Model trunk = mb.createCylinder(0.4f, 3f, 0.4f, 8,
                new Material(ColorAttribute.createDiffuse(new Color(0.42f, 0.28f, 0.15f, 1f))),
                Usage.Position | Usage.Normal);
            worldModels.add(trunk);
            ModelInstance trunkI = new ModelInstance(trunk);
            trunkI.transform.setToTranslation(tx, 1.5f, tz);
            worldInstances.add(trunkI);

            // Daun (sphere)
            Model leaf = mb.createSphere(3f, 3f, 3f, 12, 8,
                new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.6f, 0.15f, 1f))),
                Usage.Position | Usage.Normal);
            worldModels.add(leaf);
            ModelInstance leafI = new ModelInstance(leaf);
            leafI.transform.setToTranslation(tx, 4f, tz);
            worldInstances.add(leafI);
        }
    }

    private void addBorderWall(ModelBuilder mb, Color color,
                               float x, float y, float z,
                               float w, float h, float d) {
        if (w <= 0) w = 0.5f;
        if (d <= 0) d = 0.5f;
        Model wall = mb.createBox(w, h, d,
            new Material(ColorAttribute.createDiffuse(color)),
            Usage.Position | Usage.Normal);
        worldModels.add(wall);
        ModelInstance wi = new ModelInstance(wall);
        wi.transform.setToTranslation(x, h/2f, z);
        worldInstances.add(wi);
    }

    public void render(PerspectiveCamera cam) {
        // Render Map 3D yang baru
        if (sceneManager != null) {
            sceneManager.setCamera(cam);
            sceneManager.update(Gdx.graphics.getDeltaTime());
            sceneManager.render();
        }

        // Render Map/Properti Lama
        modelBatch.begin(cam);
        for (ModelInstance mi : worldInstances) {
            modelBatch.render(mi, environment);
        }
        modelBatch.end();

        // HAPUS RENDER MUSUH LAMA INI
        // for (SukmaGowong enemy : enemies) {
        //     enemy.render(cam);
        // }
    }

    public void dispose() {
        modelBatch.dispose();
        for (Model m : worldModels) { if (m != null) m.dispose(); }
        worldModels.clear();

        // Buang memori GLTF
        if (sceneManager != null) sceneManager.dispose();
        if (mapAsset != null) mapAsset.dispose();

        // HAPUS DISPOSE MUSUH LAMA
        // for (SukmaGowong enemy : enemies) {
        //     enemy.dispose();
        // }
    }
}
