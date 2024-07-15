package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.World;

public abstract class Entity implements Drawable, Updatable, Disposable {
    protected final AssetManager assets;
    protected final Batch batch;
    protected final Vector2 position;
    protected final World world;
    protected boolean alive = true;

    public Entity(World world, Vector2 position) {
        this.world = world;
        world.addEntity(this);
        this.assets = world.getAssets();
        this.batch = world.getBatch();
        this.position = position.cpy();
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void dispose() {
        alive = false;
    }
}
