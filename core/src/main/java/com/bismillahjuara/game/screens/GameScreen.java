package com.bismillahjuara.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.List;

/**
 * GameScreen — fondasi yang solid untuk 3D game libGDX.
 *
 * KONTROL PC:
 *   WASD          = Gerak relatif ke arah hadap kamera
 *   Shift         = Sprint (2x speed)
 *   Mouse kiri drag = Rotate kamera (orbit di sekitar player)
 *   Mouse scroll  = Zoom in/out
 *   Esc           = (placeholder: pause / keluar)
 *
 * KONTROL MOBILE:
 *   Joystick kiri (touch area kiri)  = Gerak
 *   Drag di area kanan               = Rotate kamera
 *   Pinch                            = Zoom
 *
 * MAP:
 *   - Ground tile berwarna-warni (hijau / coklat / stone pattern)
 *   - Obstacle boxes tersebar sebagai placeholder props
 *   - Langit biru clear dengan fog jauh
 */
public class GameScreen implements Screen {

    // =========================================================
    //  CONSTANTS
    // =========================================================
    private static final float PLAYER_SPEED        = 8f;
    private static final float PLAYER_SPRINT_MULT  = 2.2f;
    private static final float PLAYER_HEIGHT       = 1f;       // Y offset dari tanah

    private static final float CAM_DISTANCE_DEFAULT = 10f;
    private static final float CAM_DISTANCE_MIN     = 3f;
    private static final float CAM_DISTANCE_MAX     = 25f;
    private static final float CAM_PITCH_MIN        = 5f;      // derajat — jangan terlalu rendah
    private static final float CAM_PITCH_MAX        = 80f;     // derajat — jangan terlalu tinggi
    private static final float CAM_ROTATE_SPEED     = 0.3f;    // per pixel drag
    private static final float CAM_ZOOM_SPEED       = 1.5f;
    private static final float CAM_SMOOTH           = 8f;      // lerp factor

    // =========================================================
    //  3D RENDERING
    // =========================================================
    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    private Environment environment;

    // =========================================================
    //  PLAYER
    // =========================================================
    private ModelInstance playerInstance;
    private Model playerModel;
    private Vector3 playerPos   = new Vector3(0, PLAYER_HEIGHT, 0);
    private Vector3 playerVel   = new Vector3();

    // =========================================================
    //  MAP / WORLD
    // =========================================================
    private Array<ModelInstance> worldInstances = new Array<>();
    private List<Model> worldModels = new ArrayList<>();

    // =========================================================
    //  KAMERA ORBIT STATE
    // =========================================================
    private float camYaw       = 0f;    // sudut horizontal (derajat)
    private float camPitch     = 30f;   // sudut vertikal (derajat)
    private float camDistance  = CAM_DISTANCE_DEFAULT;
    // Target smooth
    private float targetYaw    = 0f;
    private float targetPitch  = 30f;
    private float targetDist   = CAM_DISTANCE_DEFAULT;

    // =========================================================
    //  INPUT — PC
    // =========================================================
    private boolean isDragging      = false;
    private int     lastDragX, lastDragY;

    // =========================================================
    //  INPUT — MOBILE (Virtual Joystick + Camera Touch)
    // =========================================================
    private static final boolean IS_MOBILE = (Gdx.app != null) &&
        (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
            Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS);

    // Joystick state
    private boolean  joystickActive    = false;
    private int      joystickPointer   = -1;
    private Vector2  joystickCenter    = new Vector2();
    private Vector2  joystickCurrent   = new Vector2();
    private static final float JOYSTICK_RADIUS  = 80f;
    private static final float JOYSTICK_ZONE_W  = 0.45f; // 45% layar kiri untuk joystick

    // Camera touch (area kanan)
    private boolean  camTouchActive    = false;
    private int      camTouchPointer   = -1;
    private float    camTouchLastX, camTouchLastY;

    // Pinch zoom
    private boolean  pinching          = false;
    private float    pinchLastDist     = 0f;

    // =========================================================
    //  HUD (2D overlay)
    // =========================================================
    private SpriteBatch spriteBatch;
    private BitmapFont  font;

    // =========================================================
    //  INTERNAL
    // =========================================================
    private final Vector3 tmpV3 = new Vector3();

    // ============================================================
    public GameScreen() {}

    // ============================================================
    @Override
    public void show() {
        modelBatch  = new ModelBatch();
        spriteBatch = new SpriteBatch();
        font        = new BitmapFont();
        font.setColor(Color.WHITE);

        setupEnvironment();
        setupCamera();
        buildWorld();
        setupInput();
    }

    // ─────────────────────────────────────────────────────────
    private void setupEnvironment() {
        environment = new Environment();
        // Ambient yang hangat sedikit
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.50f, 1f));
        // Directional utama — arah sore hari
        environment.add(new DirectionalLight().set(1.0f, 0.95f, 0.85f, -1f, -1f, -0.4f));
        // Directional fill dari bawah agar tidak terlalu gelap
        environment.add(new DirectionalLight().set(0.2f, 0.22f, 0.25f,  0.5f, 0.5f, 0.2f));
    }

    // ─────────────────────────────────────────────────────────
    private void setupCamera() {
        cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.2f;
        cam.far  = 200f;
        updateCameraPosition(1f);
    }

    // ─────────────────────────────────────────────────────────
    /**
     * Bangun map:
     *   - Ground tiles 5x5 unit dengan variasi warna
     *   - Ring batu di tepi sebagai "dinding"
     *   - Beberapa obstacle box tersebar
     *   - Pohon-pohon silinder placeholder
     */
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

        // --- PLAYER MODEL ---
        playerModel = mb.createBox(1f, 2f, 1f,
            new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.5f, 1f, 1f))),
            Usage.Position | Usage.Normal);
        playerInstance = new ModelInstance(playerModel);
        playerInstance.transform.setToTranslation(playerPos);
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

    // ─────────────────────────────────────────────────────────
    private void setupInput() {
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) { return false; }
            @Override
            public boolean keyUp(int keycode) { return false; }
            @Override
            public boolean keyTyped(char character) { return false; }

            // ── Mouse (PC) ──────────────────────────────────
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (!IS_MOBILE) {
                    if (button == Input.Buttons.LEFT) {
                        isDragging = true;
                        lastDragX = screenX;
                        lastDragY = screenY;
                        return true;
                    }
                } else {
                    return handleMobileTouchDown(screenX, screenY, pointer);
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (!IS_MOBILE) {
                    if (button == Input.Buttons.LEFT) {
                        isDragging = false;
                        return true;
                    }
                } else {
                    return handleMobileTouchUp(screenX, screenY, pointer);
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (!IS_MOBILE) {
                    if (isDragging) {
                        float dx = screenX - lastDragX;
                        float dy = screenY - lastDragY;
                        targetYaw   -= dx * CAM_ROTATE_SPEED;
                        targetPitch += dy * CAM_ROTATE_SPEED;
                        targetPitch  = MathUtils.clamp(targetPitch, CAM_PITCH_MIN, CAM_PITCH_MAX);
                        lastDragX = screenX;
                        lastDragY = screenY;
                        return true;
                    }
                } else {
                    return handleMobileTouchDragged(screenX, screenY, pointer);
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) { return false; }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                targetDist += amountY * CAM_ZOOM_SPEED;
                targetDist  = MathUtils.clamp(targetDist, CAM_DISTANCE_MIN, CAM_DISTANCE_MAX);
                return true;
            }

            // ── Touch events (wired ke mobile handler) ──────
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                if (IS_MOBILE) handleMobileTouchUp(screenX, screenY, pointer);
                return false;
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    //  MOBILE INPUT HANDLERS
    // ─────────────────────────────────────────────────────────
    private boolean handleMobileTouchDown(int sx, int sy, int pointer) {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // Pinch — dua jari sudah ada
        if (joystickActive && pointer != joystickPointer && sx > screenW * JOYSTICK_ZONE_W) {
            // cek apakah sudah ada cam touch → mulai pinch
            if (camTouchActive) {
                pinching = true;
                float x1 = camTouchLastX, y1 = camTouchLastY;
                float x2 = sx, y2 = sy;
                pinchLastDist = Vector2.dst(x1, y1, x2, y2);
                return true;
            }
        }

        if (sx < screenW * JOYSTICK_ZONE_W && !joystickActive) {
            joystickActive  = true;
            joystickPointer = pointer;
            joystickCenter.set(sx, screenH - sy);
            joystickCurrent.set(sx, screenH - sy);
            return true;
        } else if (sx >= screenW * JOYSTICK_ZONE_W && !camTouchActive) {
            camTouchActive  = true;
            camTouchPointer = pointer;
            camTouchLastX   = sx;
            camTouchLastY   = sy;
            return true;
        }
        return false;
    }

    private boolean handleMobileTouchUp(int sx, int sy, int pointer) {
        if (pointer == joystickPointer) {
            joystickActive  = false;
            joystickPointer = -1;
            joystickCurrent.set(joystickCenter);
        }
        if (pointer == camTouchPointer) {
            camTouchActive  = false;
            camTouchPointer = -1;
            pinching = false;
        }
        return false;
    }

    private boolean handleMobileTouchDragged(int sx, int sy, int pointer) {
        float screenH = Gdx.graphics.getHeight();

        if (pinching) {
            // update distance salah satu jari lalu hitung pinch
            if (pointer == camTouchPointer) {
                float x1 = camTouchLastX, y1 = (screenH - camTouchLastY);
                float x2 = sx, y2 = (screenH - (float)sy);
                // ambil distance
                // (sederhana: hanya gunakan 1 titik sebagai approx)
                float newDist = Vector2.dst(x1, y1, x2, y2);
                float delta = (pinchLastDist - newDist) * 0.05f;
                targetDist += delta;
                targetDist  = MathUtils.clamp(targetDist, CAM_DISTANCE_MIN, CAM_DISTANCE_MAX);
                pinchLastDist = newDist;
            }
            return true;
        }

        if (pointer == joystickPointer && joystickActive) {
            joystickCurrent.set(sx, screenH - sy);
            return true;
        }
        if (pointer == camTouchPointer && camTouchActive) {
            float dx = sx - camTouchLastX;
            float dy = sy - camTouchLastY;
            targetYaw   -= dx * CAM_ROTATE_SPEED;
            targetPitch += dy * CAM_ROTATE_SPEED;
            targetPitch  = MathUtils.clamp(targetPitch, CAM_PITCH_MIN, CAM_PITCH_MAX);
            camTouchLastX = sx;
            camTouchLastY = sy;
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f); // clamp agar tidak glitch saat lag

        handleMovement(delta);
        smoothCamera(delta);
        updateCameraPosition(delta);

        // Clear
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.98f, 1f); // langit biru cerah
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(cam);
        for (ModelInstance mi : worldInstances) {
            modelBatch.render(mi, environment);
        }
        modelBatch.render(playerInstance, environment);
        modelBatch.end();

        // HUD
        renderHUD();
    }

    // ─────────────────────────────────────────────────────────
    /**
     * Gerakan player:
     * - PC: WASD relatif ke arah kamera (yaw saja, bukan pitch)
     * - Mobile: Joystick vector relatif ke kamera
     */
    private void handleMovement(float delta) {
        Vector2 moveInput = new Vector2();

        if (!IS_MOBILE) {
            // ── PC INPUT ──
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
                moveInput.y += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
                moveInput.y -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
                moveInput.x -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                moveInput.x += 1;
        } else {
            // ── MOBILE JOYSTICK ──
            if (joystickActive) {
                Vector2 jDelta = new Vector2(joystickCurrent).sub(joystickCenter);
                float jLen = jDelta.len();
                if (jLen > JOYSTICK_RADIUS) jDelta.nor().scl(JOYSTICK_RADIUS);
                moveInput.set(jDelta.x / JOYSTICK_RADIUS, jDelta.y / JOYSTICK_RADIUS);
            }
        }

        if (moveInput.len2() > 0.01f) {
            moveInput.nor(); // normalize agar diagonal tidak lebih cepat
            float speed = PLAYER_SPEED;
            if (!IS_MOBILE && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                speed *= PLAYER_SPRINT_MULT;
            }

            // Arah maju = berdasarkan yaw kamera (horizontal only)
            float yawRad = MathUtils.degreesToRadians * camYaw;
            float forwardX = -MathUtils.sin(yawRad);
            float forwardZ = -MathUtils.cos(yawRad);
            float rightX   =  MathUtils.cos(yawRad);
            float rightZ   = -MathUtils.sin(yawRad);

            playerPos.x += (forwardX * moveInput.y + rightX * moveInput.x) * speed * delta;
            playerPos.z += (forwardZ * moveInput.y + rightZ * moveInput.x) * speed * delta;

            // Bounding ke dalam map
            float mapLimit = 48f;
            playerPos.x = MathUtils.clamp(playerPos.x, -mapLimit, mapLimit);
            playerPos.z = MathUtils.clamp(playerPos.z, -mapLimit, mapLimit);
        }

        playerPos.y = PLAYER_HEIGHT;
        playerInstance.transform.setToTranslation(playerPos);
    }

    // ─────────────────────────────────────────────────────────
    /** Smooth / lerp camera values */
    private void smoothCamera(float delta) {
        float t = Math.min(CAM_SMOOTH * delta, 1f);
        camYaw      = lerpAngle(camYaw,   targetYaw,   t);
        camPitch    += (targetPitch - camPitch) * t;
        camDistance += (targetDist  - camDistance) * t;
    }

    /** Hitung posisi kamera orbit di sekitar player */
    private void updateCameraPosition(float delta) {
        float pitchRad = MathUtils.degreesToRadians * camPitch;
        float yawRad   = MathUtils.degreesToRadians * camYaw;

        float hDist = camDistance * MathUtils.cos(pitchRad);
        float vDist = camDistance * MathUtils.sin(pitchRad);

        float camX = playerPos.x + hDist * MathUtils.sin(yawRad);
        float camZ = playerPos.z + hDist * MathUtils.cos(yawRad);
        float camY = playerPos.y + 0.5f + vDist; // offset sedikit di atas kepala

        cam.position.set(camX, camY, camZ);
        cam.lookAt(playerPos.x, playerPos.y + 0.5f, playerPos.z);
        cam.up.set(Vector3.Y);
        cam.update();
    }

    /** Lerp sudut dengan wraparound 360 */
    private float lerpAngle(float a, float b, float t) {
        float diff = b - a;
        while (diff > 180)  diff -= 360;
        while (diff < -180) diff += 360;
        return a + diff * t;
    }

    // ─────────────────────────────────────────────────────────
    /** Render HUD: info di pojok layar + virtual joystick di mobile */
    private void renderHUD() {
        spriteBatch.begin();

        // Debug info (atas-kiri)
        font.draw(spriteBatch,
            String.format("Pos: %.1f, %.1f | Yaw: %.1f", playerPos.x, playerPos.z, camYaw),
            10, Gdx.graphics.getHeight() - 10);

        if (!IS_MOBILE) {
            font.draw(spriteBatch,
                "WASD = Gerak | Shift = Sprint | LMB drag = Kamera | Scroll = Zoom",
                10, 20);
        }

        // Virtual joystick (mobile)
        if (IS_MOBILE) {
            drawVirtualJoystick();
        }

        spriteBatch.end();
    }

    /**
     * Gambar joystick sederhana dengan ShapeRenderer-style menggunakan BitmapFont circle trick.
     * Untuk tampilan yang lebih bagus, ganti dengan Texture atlas joystick.
     * Saat ini digambar secara minimal menggunakan teks placeholder.
     */
    private void drawVirtualJoystick() {
        float cx = joystickActive ? joystickCenter.x  : Gdx.graphics.getWidth()  * 0.15f;
        float cy = joystickActive ? joystickCenter.y  : Gdx.graphics.getHeight() * 0.2f;

        // Label sederhana — ganti dengan Texture lingkaran joystick untuk tampilan real
        font.setColor(new Color(1, 1, 1, 0.6f));
        font.draw(spriteBatch, "[JOYSTICK]", cx - 35, cy);
        if (joystickActive) {
            float knobX = joystickCurrent.x;
            float knobY = joystickCurrent.y;
            font.draw(spriteBatch, "●", knobX - 5, knobY);
        }
        font.setColor(Color.WHITE);
    }

    // ─────────────────────────────────────────────────────────
    @Override
    public void resize(int width, int height) {
        cam.viewportWidth  = width;
        cam.viewportHeight = height;
        cam.update();
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        modelBatch.dispose();
        spriteBatch.dispose();
        font.dispose();
        if (playerModel != null) playerModel.dispose();
        for (Model m : worldModels) { if (m != null) m.dispose(); }
        worldModels.clear();
    }
}
