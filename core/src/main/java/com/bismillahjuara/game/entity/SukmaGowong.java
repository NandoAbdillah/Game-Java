package com.bismillahjuara.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * SukmaGowong - Enemy tipe Chaser.
 * Memiliki state AI: Mendekat pelan (Crawl) -> Lari cepat (Run) -> Serang (Melee) -> Mati (Die).
 */
public class SukmaGowong extends Entity {

    // --- ENUM UNTUK STATE MACHINE (AI) ---
    public enum State {
        IDLE, CRAWL, CRAWL_RUN, RUN, MELEE, DIE, DEAD
    }

    // --- ATRIBUT AI & RADIUS ---
    private State currentState = State.IDLE;
    private float detectRadius = 15f; // Jarak mulai melihat player (Crawl)
    private float runRadius    = 5f; // Jarak mulai ngejar cepat (Run)
    private float meleeRadius  = 1.5f; // Jarak untuk memukul (Melee)

    private float crawlSpeed = 2f;
    private float runSpeed   = 5.5f;

    // --- ATRIBUT TIMER & COMBAT ---
    private float stateTimer = 0f;
    private float meleeDurationLimit = 4f; // Setelah 4 detik melee, dia akan otomatis mati (Die)
    private float health = 100f; // TODO: Implementasi sistem damage dari player nanti

    private float scale = 0.5f;

    // --- ATRIBUT 3D GLTF ---
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene enemyScene;
    private AnimationController animationController;

    public SukmaGowong(Vector3 startPos) {
        super(startPos); // Memanggil constructor dari Entity (mengisi this.position)
        setupGLTF();
    }

    private void setupGLTF() {
        // Menggunakan DefaultShader seperti Player agar warnanya tidak bleaching/pucat
        DefaultShader.Config config = new DefaultShader.Config();
        config.numBones = 80;
        DepthShader.Config depthConfig = new DepthShader.Config();
        depthConfig.numBones = 80;

        sceneManager = new SceneManager(
            new DefaultShaderProvider(config),
            new DepthShaderProvider(depthConfig)
        );

        sceneManager.setAmbientLight(0.6f);
        DirectionalLightEx sunLight = new DirectionalLightEx();
        sunLight.direction.set(-1f, -1f, -0.4f).nor();
        sunLight.color.set(Color.WHITE);
        sunLight.intensity = 1.0f;
        sceneManager.environment.add(sunLight);

        // TODO: Ganti path ini dengan model asli SukmaGowong milikmu (jika sudah ada)
        // Sementara pakai dummy/placeholder agar tidak error
        try {
            sceneAsset = new GLBLoader().load(Gdx.files.internal("models/enemies/SukmaGowong.glb"));
        } catch (Exception e) {
            // Fallback jika file belum ada, pakai model player atau apapun yang tersedia sementara
            System.out.println("Model SukmaGowong belum ada, menggunakan model default.");
            sceneAsset = new GLBLoader().load(Gdx.files.internal("models/chars/SukmaGowong.glb"));
        }

        enemyScene = new Scene(sceneAsset.scene);

        // Fix Material Anti Abu-abu
        for (Material material : enemyScene.modelInstance.materials) {
            material.remove(BlendingAttribute.Type);
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(GL20.GL_BACK));
            material.set(ColorAttribute.createDiffuse(Color.WHITE));
        }

        enemyScene.modelInstance.transform
            .setToTranslation(position)
            .scale(scale, scale, scale);

        animationController = new AnimationController(enemyScene.modelInstance);
        changeState(State.IDLE); // Set animasi awal

        sceneManager.addScene(enemyScene);
    }

    // --- FUNGSI UPDATE LOGIKA AI ---
    // Menerima Vector3 posisi player (Bisa diganti menerima Objek Player langsung nantinya)
    public void updateAI(float delta, Vector3 playerPos) {
        if (currentState == State.DEAD) return; // Jika sudah jadi mayat, jangan lakukan apa-apa

        float distanceToPlayer = position.dst(playerPos);

        // Hitung arah hadap (Rotasi) ke player
        float dirX = playerPos.x - position.x;
        float dirZ = playerPos.z - position.z;
        this.yaw = MathUtils.atan2(dirX, dirZ) * MathUtils.radiansToDegrees;

        // --- STATE MACHINE LOGIC ---
        switch (currentState) {
            case IDLE:
                if (distanceToPlayer <= detectRadius && distanceToPlayer > runRadius) {
                    changeState(State.CRAWL);
                } else if (distanceToPlayer <= runRadius) {
                    changeState(State.RUN);
                }
                break;

            case CRAWL:
                moveTowardsPlayer(dirX, dirZ, distanceToPlayer, crawlSpeed, delta);
                if (distanceToPlayer <= runRadius) changeState(State.RUN);
                if (distanceToPlayer > detectRadius) changeState(State.IDLE); // Player kabur jauh
                break;

            case RUN:
                moveTowardsPlayer(dirX, dirZ, distanceToPlayer, runSpeed, delta);
                if (distanceToPlayer <= meleeRadius) changeState(State.MELEE);
                break;

            case MELEE:
                // TODO: Panggil fungsi player.takeDamage() di sini jika sudah ada

                stateTimer += delta;
                // Jika sudah nyerang selama beberapa detik, musuh akan mati sendiri
                if (stateTimer >= meleeDurationLimit) {
                    changeState(State.DIE);
                } else if (distanceToPlayer > meleeRadius) {
                    changeState(State.RUN); // Player menghindar, kejar lagi!
                }
                break;

            case DIE:
                stateTimer += delta;
                // Asumsi durasi animasi mati adalah 2 detik
                if (stateTimer >= 2.0f) {
                    changeState(State.DEAD);
                }
                break;
        }

        // Terapkan posisi dan rotasi ke model 3D
        if (enemyScene != null) {
            enemyScene.modelInstance.transform
                .setToTranslation(position)
                .rotate(Vector3.Y, yaw)
                .scale(scale, scale, scale); // <--- INI FIX-NYA! Wajib pasang skala lagi di sini
        }

        if (animationController != null) animationController.update(delta);
    }

    private void moveTowardsPlayer(float dirX, float dirZ, float distance, float speed, float delta) {
        // Normalisasi vektor arah
        float normX = dirX / distance;
        float normZ = dirZ / distance;

        this.position.x += normX * speed * delta;
        this.position.z += normZ * speed * delta;

        // TODO: Tambahkan logika collision map/tembok di sini nanti
    }

    private void changeState(State newState) {
        if (currentState == newState) return;
        this.currentState = newState;
        this.stateTimer = 0f;

        // Ubah animasi berdasarkan state baru
        // (Pastikan nama animasi sesuai dengan yang ada di file Blender kamu)
        try {
            switch (newState) {
                case IDLE:  animationController.animate("Idle", -1, 1f, null, 0.2f); break;
                case CRAWL: animationController.animate("Crawl", -1, 1f, null, 0.2f); break;
                case RUN:   animationController.animate("Run", -1, 1f, null, 0.2f); break;
                case MELEE: animationController.animate("Melee", -1, 1f, null, 0.2f); break;
                case DIE:   animationController.animate("Die", 1, 1f, null, 0.2f); break; // 1 kali putaran saja
                case DEAD:  /* Tidak ada animasi baru, biarkan di frame terakhir Die */ break;
                default: break;
            }
        } catch (Exception e) {
            // Abaikan error jika nama animasi belum ada/salah ketik
        }
    }

    @Override
    public void update(float delta) {
        // Implementasi wajib dari Entity, tapi kita pakai updateAI agar lebih spesifik
    }

    public void render(PerspectiveCamera cam) {
        if (sceneManager != null) {
            sceneManager.setCamera(cam);
            sceneManager.update(Gdx.graphics.getDeltaTime());
            sceneManager.render();
        }
    }

    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (sceneAsset != null) sceneAsset.dispose();
    }
}
