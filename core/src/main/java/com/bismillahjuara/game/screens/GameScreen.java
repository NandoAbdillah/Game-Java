package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.bismillahjuara.game.camera.AdvancedCameraSystem;
import com.bismillahjuara.game.core.GameplayManager;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;

/**
 * Screen Gameplay Modern (AAA Architecture).
 * Tidak ada logika fisika, tidak ada GL Clear, hanya orkestrasi Lifecycle.
 */
public class GameScreen implements Screen {

    private GameplayManager gameplayManager;
    private HudManager hudManager;
    private GameInputHandler inputHandler;
    private AdvancedCameraSystem camera;

    public GameScreen() {
        // Diciptakan dalam keadaan kosong oleh AsyncGameplayLoader
    }

    // --- DEFERRED INITIALIZATION PIPELINE ---

    public void initWorld() {
        gameplayManager = new GameplayManager();
        gameplayManager.buildWorld();
    }

    public void initEntities() {
        camera = new AdvancedCameraSystem();
        gameplayManager.buildEntities(camera);
    }

    public void initUI() {
        hudManager = new HudManager();
        inputHandler = new GameInputHandler(camera, hudManager);
        gameplayManager.bindInput(inputHandler);
    }

    // ---------------------------------------

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (hudManager != null) multiplexer.addProcessor(hudManager.getStage());
        if (inputHandler != null) multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        // Tandai bahwa loading selesai dan game dimulai
        gameplayManager.startGameplay();
    }

    @Override
    public void render(float delta) {
        if (gameplayManager == null) return;

        // 1. UPDATE (Update input, physics, HUD, AI)
        inputHandler.update(delta);
        gameplayManager.update(delta);

        // 2. RENDER (Gambar 3D dan 2D UI)
        gameplayManager.render(delta);

        // FIX Anti-Crash: Gunakan Vector3.Zero bawaan LibGDX agar tidak ada alokasi memori (Garbage Collection) baru.
        // Nanti di fase UI lanjutan, HUD akan membaca posisi player langsung dari GameContext.
        hudManager.updateAndRender(com.badlogic.gdx.math.Vector3.Zero, camera.getYaw());
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) camera.resize(width, height);
        if (hudManager != null) hudManager.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        if (gameplayManager != null) gameplayManager.dispose();
        if (hudManager != null) hudManager.dispose();
    }
}
