package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.world.World;

public abstract class EntityObj extends Entity{
    protected Sprite sprite;
    protected final Vector2 position;
    public EntityObj(World world, Vector2 position) {
        super(world);
        this.position = position.cpy();
    }

    public Vector2 getPosition() {
        return position;
    }
}
