package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import com.bismillahjuara.game.camera.OrbitCamera;
import com.bismillahjuara.game.entity.Player;
import com.bismillahjuara.game.hud.HudManager;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.world.WorldMap;

public class GameScreen implements Screen {

    private OrbitCamera camera;
    private GameInputHandler inputHandler;
    private Player player;
    private WorldMap world;
    private HudManager hudManager;

    public GameScreen() {}

    @Override
    public void show() {
        // 1. Inisialisasi HUD (UI) lebih dulu
        hudManager = new HudManager();

        // 2. Inisialisasi Kamera & Input Manager
        camera = new OrbitCamera();
        inputHandler = new GameInputHandler(camera, hudManager);

        // 3. MULTIPLEXER (Best Practice LibGDX)
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudManager.getStage()); // UI menyerap input lebih dulu
        multiplexer.addProcessor(inputHandler);          // Sisanya dilempar ke Kamera 3D
        Gdx.input.setInputProcessor(multiplexer);

        // 4. Inisialisasi Dunia dan Karakter
        world = new WorldMap();
        player = new Player(camera);

        // CATATAN ENGINEER:
        // Kita TIDAK LAGI memanggil inputHandler.setPlayer(player).
        // Karena sekarang kita memakai Data-Driven Input Architecture.
    }

    @Override
    public void render(float delta) {
        // Failsafe: Cegah "Spiral of Death" jika frame drop parah (Best practice Physics Engine)
        delta = Math.min(delta, 0.05f);

        // ==========================================
        // --- DATA-DRIVEN LOGIC PIPELINE ---
        // ==========================================
        // 1. Input Controller membaca hardware dan memperbarui state "InputAction"
        inputHandler.update(delta);

        // 2. Player mengonsumsi "InputAction" untuk bergerak dan animasi
        player.processInputAndPhysics(inputHandler.getAction(), camera.getYaw(), delta);

        // 3. AI & Kamera mengikuti Player
        world.updateEnemies(delta, player.getPosition());
        camera.update(player.getPosition(), delta);

        // ==========================================
        // --- RENDER PIPELINE ---
        // ==========================================
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        world.render(camera.getCam());
        player.render();

        // RENDER 2D UI (HARUS PALING TERAKHIR)
        hudManager.updateAndRender(player.getPosition(), camera.getYaw());
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        hudManager.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        // Mencegah Memory Leak (Best Practice Lifecycle)
        player.dispose();
        world.dispose();
        hudManager.dispose();
    }
}
