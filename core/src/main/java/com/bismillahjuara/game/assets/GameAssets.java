package com.bismillahjuara.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.bismillahjuara.game.audio.AudioSFX;
import com.bismillahjuara.game.audio.AudioTrack;

import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameAssets {

    private static GameAssets instance;
    public AssetManager manager;

    // ASSET
    public static final String MAP_GLB = "models/maps/Maps.glb";
    public static final String PLAYER_GLB = "models/chars/TimunAnim2.glb";
    public static final String ENEMY_GLB = "models/chars/SukmaGowong.glb";
    public static  final String BUTO_GLB = "models/chars/ButoIjo.glb";

    public static final String HOME_GLB = "models/maps/Home.glb";
    public static final String TEMPLE_GLB = "models/maps/Temple.glb";

    public static final String KERIS_GLB = "models/object/Keris.glb";
    public static final String KUJANG_GLB = "models/object/Kujang.glb";
    public static final String MANDAU_GLB = "models/object/Mandau.glb";

    private GameAssets() {
        manager = new AssetManager();
        manager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader(new InternalFileHandleResolver()));
    }

    public static GameAssets getInstance() {
        if (instance == null) instance = new GameAssets();
        return instance;
    }

    public void queueBootAssets() {
        safeLoadSound(AudioSFX.UI_CLICK.path);
        safeLoadSound(AudioSFX.UI_HOVER.path);
        safeLoadSound(AudioSFX.UI_BACK.path);
        safeLoadMusic(AudioSFX.HEARTBEAT.path);
        safeLoadMusic(AudioSFX.SUKMA_01.path);
        safeLoadMusic(AudioSFX.SUKMA_02.path);
        safeLoadMusic(AudioTrack.THEME.path);
        safeLoadMusic(AudioTrack.CREDITS_THEME.path);
        safeLoadMusic(AudioTrack.FOREST_AMBIENT.path);

    }

    public void queueGameplayAssets() {
        for (AudioTrack track : AudioTrack.values()) {
            if (!manager.isLoaded(track.path)) safeLoadMusic(track.path);
        }
        for (AudioSFX sfx : AudioSFX.values()) {
            if (!manager.isLoaded(sfx.path)) safeLoadSound(sfx.path);
        }

        // 3D MODELS (ASYNC LOADING)
        safeLoadGLB(MAP_GLB);
        safeLoadGLB(PLAYER_GLB);
        safeLoadGLB(ENEMY_GLB);
        safeLoadGLB(HOME_GLB);
        safeLoadGLB(TEMPLE_GLB);
        safeLoadGLB(BUTO_GLB);

        safeLoadGLB(KERIS_GLB);
        safeLoadGLB(KUJANG_GLB);
        safeLoadGLB(MANDAU_GLB);
    }

    private void safeLoadGLB(String path) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, SceneAsset.class);
        } else {
            Gdx.app.error("ASSETS", "Model 3D tidak ditemukan: " + path);
        }
    }

    private void safeLoadMusic(String path) {
        if (Gdx.files.internal(path).exists()) manager.load(path, Music.class);
    }

    private void safeLoadSound(String path) {
        if (Gdx.files.internal(path).exists()) manager.load(path, Sound.class);
    }

    public void dispose() {
        if (manager != null) manager.dispose();
    }
}
