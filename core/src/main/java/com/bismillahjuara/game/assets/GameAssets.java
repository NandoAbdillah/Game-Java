package com.bismillahjuara.game.assets;

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

    // --- ASSET PATHS ---
    public static final String MAP_GLB = "models/maps/Maps.glb";
    public static final String PLAYER_GLB = "models/chars/TimunAnim2.glb";
    public static final String ENEMY_GLB = "models/chars/SukmaGowong.glb"; // Atau models/enemies/SukmaGowong.glb

    private GameAssets() {
        manager = new AssetManager();
        // DAFTARKAN GLB LOADER AGAR BISA DIBACA DI BACKGROUND THREAD!
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
        safeLoadMusic(AudioTrack.THEME.path);
    }

    public void queueGameplayAssets() {
        // AUDIO
        for (AudioTrack track : AudioTrack.values()) {
            if (!manager.isLoaded(track.path)) safeLoadMusic(track.path);
        }
        for (AudioSFX sfx : AudioSFX.values()) {
            if (!manager.isLoaded(sfx.path)) safeLoadSound(sfx.path);
        }

        // 3D MODELS (ASYNC LOADING)
        manager.load(MAP_GLB, SceneAsset.class);
        manager.load(PLAYER_GLB, SceneAsset.class);
        manager.load(ENEMY_GLB, SceneAsset.class);
    }

    private void safeLoadMusic(String path) {
        if (com.badlogic.gdx.Gdx.files.internal(path).exists()) manager.load(path, Music.class);
    }

    private void safeLoadSound(String path) {
        if (com.badlogic.gdx.Gdx.files.internal(path).exists()) manager.load(path, Sound.class);
    }

    public void dispose() {
        if (manager != null) manager.dispose();
    }
}
