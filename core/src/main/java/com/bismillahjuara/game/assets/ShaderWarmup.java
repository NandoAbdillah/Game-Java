package com.bismillahjuara.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * Memaksa OpenGL untuk meng-compile Shader yang berat selama layar Loading masih menyala. Mencegah "First-Frame Lag Spike"
 */
public class ShaderWarmup {

    private static SceneManager warmupSceneManager;

    /** Dipanggil tepat saat loading mencapai 100%, sebelum masuk ke Main Menu */
    public static void executeWarmup() {
        Gdx.app.log("WARMUP", "Memulai kompilasi Shader 3D PBR/Default...");

        long startTime = System.currentTimeMillis();

        // menginisialisasi SceneManager di sini akan memaksa LibGDX membuat dan mengkompilasi Shader Program
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;
        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        warmupSceneManager = new SceneManager(
            new DefaultShaderProvider(config),
            new DepthShaderProvider(depthConfig)
        );

        long timeTaken = System.currentTimeMillis() - startTime;
        Gdx.app.log("WARMUP", "Kompilasi selesai dalam " + timeTaken + " ms.");

        // TODO: Future Up - Spawn 1 model musuh dan player off-screen untuk memastikan texture cache masuk ke VRAM
    }

    public static SceneManager getPrecompiledSceneManager() {
        return warmupSceneManager;
    }
}
