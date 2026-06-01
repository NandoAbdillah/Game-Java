package com.bismillahjuara.game.managers;

import com.badlogic.gdx.utils.Array;
import com.bismillahjuara.game.core.GameContext;
import com.bismillahjuara.game.entity.Entity;


public class EntityManager {

    private GameContext context;

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

    public Array<Entity> getEntities() {
        return entities;
    }

    public void update(float fixedDelta) {
        if (pendingRemovals.size > 0) {
            entities.removeAll(pendingRemovals, true);
            pendingRemovals.clear();
        }

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);
            e.update(fixedDelta);
        }
    }

    public void dispose() {
        entities.clear();
    }
}
