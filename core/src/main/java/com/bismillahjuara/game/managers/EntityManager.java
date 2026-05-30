package com.bismillahjuara.game.managers;

import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.entity.Entity;


public class EntityManager {

    private GameContext context;

    // LibGDX Array dioptimasi untuk Mobile, jangan pakai java.util.List!
    private Array<Entity> entities;
    private Array<Entity> pendingRemovals;

    public EntityManager(GameContext context) {
        this.context = context;
        entities = new Array<>(false, 64);
        pendingRemovals = new Array<>(false, 16);
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        pendingRemovals.add(entity);
    }

    // AAA FIX: Membuka akses entitas untuk sistem Collision peluru/senjata
    public Array<Entity> getEntities() {
        return entities;
    }

    public void update(float fixedDelta) {
        // 1. Bersihkan entitas yang mati di frame sebelumnya
        if (pendingRemovals.size > 0) {
            entities.removeAll(pendingRemovals, true);
            pendingRemovals.clear();
        }

        // 2. Update logika AI & posisi semua entitas
        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);
            e.update(fixedDelta); // Tiap entitas menjalankan otak AI-nya
        }
    }

    public void dispose() {
        // TODO: Pastikan entitas memiliki method dispose jika mereka memegang aset custom
        entities.clear();
    }
}
