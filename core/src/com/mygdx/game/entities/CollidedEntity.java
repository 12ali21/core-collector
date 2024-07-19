package com.mygdx.game.entities;

import com.badlogic.gdx.math.Vector2;

public class CollidedEntity {
    public Vector2 position;
    public EntityType type;

    public CollidedEntity(Vector2 position, EntityType type) {
        this.position = position;
        this.type = type;
    }
}
