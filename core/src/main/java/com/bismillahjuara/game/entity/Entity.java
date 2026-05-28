package com.bismillahjuara.game.entity;

import com.badlogic.gdx.math.Vector3;
import com.bismillahjuara.game.core.GameContext;

/**
 * Kelas dasar (Parent) untuk semua benda hidup di dalam game (Player, Enemy, NPC).
 */
public abstract class Entity {
    protected Vector3 position;
    protected Vector3 velocity;
    protected float yaw;

    // Context untuk mengakses WorldManager
    protected GameContext context;

    // Atribut Fisika / Collision bawaan untuk semua entitas
    protected float collisionRadius = 0.5f;
    protected float collisionHeight = 2.0f;

    // Ubah constructor untuk menerima context!
    public Entity(Vector3 startPos, GameContext context) {
        this.position = new Vector3(startPos);
        this.velocity = new Vector3();
        this.yaw = 0f;
        this.context = context;
    }

    public abstract void update(float delta);

    /**
     * Fungsi Pergerakan Cerdas berbasis OOP.
     * Otomatis mengecek tabrakan dengan Tembok Gaib di WorldManager.
     * X dan Z dipisah agar karakter bisa "sliding" (meluncur di samping) saat nabrak tembok.
     */
    public void moveWithCollision(float stepX, float stepZ) {
        if (context == null || context.worldManager == null) {
            position.x += stepX;
            position.z += stepZ;
            return;
        }

        // 1. Prediksi pergerakan arah X
        Vector3 nextPosX = new Vector3(position.x + stepX, position.y, position.z);
        if (!context.worldManager.isColliding(nextPosX, collisionRadius, collisionHeight)) {
            position.x += stepX;
        }

        // 2. Prediksi pergerakan arah Z
        Vector3 nextPosZ = new Vector3(position.x, position.y, position.z + stepZ);
        if (!context.worldManager.isColliding(nextPosZ, collisionRadius, collisionHeight)) {
            position.z += stepZ;
        }
    }

    public Vector3 getPosition() { return position; }
    public float getYaw() { return yaw; }
}
