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

    public GameScreen() {
        // KOSONGKAN CONSTRUCTOR!
        // Di arsitektur AAA, kita tidak boleh melakukan pekerjaan berat saat instansiasi objek.
        // Biarkan AsyncGameplayLoader yang merakitnya satu per satu.
    }

    // ==========================================================
    // --- DEFERRED INITIALIZATION (Dipanggil oleh Pipeline Loader) ---
    // ==========================================================

    public void initWorld() {
        world = new WorldMap();
    }

    public void initEntities() {
        camera = new OrbitCamera();
        player = new Player(camera);
    }

    public void initUI() {
        hudManager = new HudManager();
        inputHandler = new GameInputHandler(camera, hudManager);
    }

    // ==========================================================

    @Override
    public void show() {
        // Method ini HANYA dipanggil oleh Engine saat loading sudah 100%
        // dan layer ini benar-benar ditampilkan di layar.

        InputMultiplexer multiplexer = new InputMultiplexer();
        // Failsafe null check (berjaga-jaga)
        if (hudManager != null) multiplexer.addProcessor(hudManager.getStage());
        if (inputHandler != null) multiplexer.addProcessor(inputHandler);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);

        // Failsafe: Jangan render jika loading belum beres
        if (player == null || world == null || inputHandler == null) return;

        // --- DATA-DRIVEN LOGIC PIPELINE ---
        inputHandler.update(delta);
        player.processInputAndPhysics(inputHandler.getAction(), camera.getYaw(), delta);

        world.updateEnemies(delta, player.getPosition());
        camera.update(player.getPosition(), delta);

        // --- RENDER PIPELINE ---
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        world.render(camera.getCam());
        player.render();

        hudManager.updateAndRender(player.getPosition(), camera.getYaw());
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) camera.resize(width, height);
        if (hudManager != null) hudManager.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        // Lepas input agar tidak nyangkut saat pindah ke layar lain
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (player != null) player.dispose();
        if (world != null) world.dispose();
        if (hudManager != null) hudManager.dispose();
    }
}
