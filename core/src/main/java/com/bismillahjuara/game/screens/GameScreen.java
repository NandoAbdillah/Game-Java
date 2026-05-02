package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;

// Import sistem buatan kita sendiri!
import com.bismillahjuara.game.camera.OrbitCamera;
import com.bismillahjuara.game.entity.Player;
import com.bismillahjuara.game.hud.HudRenderer;
import com.bismillahjuara.game.input.GameInputHandler;
import com.bismillahjuara.game.world.WorldMap;

public class GameScreen implements Screen {

    private OrbitCamera camera;
    private GameInputHandler input;
    private Player player;
    private WorldMap world;
    private HudRenderer hud;

    public GameScreen() {}

    @Override
    public void show() {
        // 1. Inisialisasi Kamera
        camera = new OrbitCamera();

        // 2. Inisialisasi Input
        input = new GameInputHandler(camera);
        Gdx.input.setInputProcessor(input); // Daftarkan pendeteksi sentuhan

        // 3. Inisialisasi Dunia dan Karakter
        world = new WorldMap();
        player = new Player(camera);

        // 4. Inisialisasi Antarmuka (UI)
        hud = new HudRenderer(input);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f); // Clamp untuk mencegah lag spike

        // --- 1. UPDATE LOGIC ---
        Vector2 moveInput = input.getMoveInput();
        boolean isSprinting = input.isSprinting();

        // Player kalkulasi posisi & animasi
        player.handleMovement(moveInput, isSprinting, camera.getYaw(), delta);

        // Kamera membuntuti player
        camera.update(player.getPosition(), delta);

        // --- 2. RENDER GRAFIS ---
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // Gambar Dunia
        world.render(camera.getCam());
        // Gambar Pemain
        player.render();
        // Gambar Joystick & Teks
        hud.render(player.getPosition(), camera.getYaw());
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
        hud.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        // Bersihkan seluruh memori dari setiap komponen
        player.dispose();
        world.dispose();
        hud.dispose();
    }
}
