package com.bismillahjuara.game.entity;

import com.badlogic.gdx.math.Vector3;

/**
 * Kelas dasar (Parent) untuk semua benda hidup di dalam game (Player, Enemy, NPC).
 */
public abstract class Entity {
    protected Vector3 position;
    protected Vector3 velocity;
    protected float yaw;

    public Entity(Vector3 startPos) {
        this.position = new Vector3(startPos);
        this.velocity = new Vector3();
        this.yaw = 0f;
    }

    public abstract void update(float delta);

    public Vector3 getPosition() { return position; }
    public float getYaw() { return yaw; }
}
