package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Disposable;

public abstract class Entity implements Drawable, Updatable, Disposable {

    protected final Bounds bounds;
    protected final AssetManager assets;
    protected final Batch batch;
    protected final World world;
    protected Sprite sprite;
    protected boolean alive = true;

    public Entity(World world) {
        this(world, new Bounds(0, 0, 0, 0));
    }

    public Entity(World world, Bounds bounds) {
        this.world = world;
        this.assets = world.getAssets();
        this.batch = world.getBatch();
        this.bounds = bounds;
    }

    public void addToWorld(boolean isStructure) {
        if (isStructure) {
            world.addStructure(this, bounds);
        } else {
            world.addEntity(this);
        }
    }

    public Sprite getGhost() {
        Sprite ghost = new Sprite(sprite);
        ghost.setColor(0.8f, 1, 0.8f, 0.5f);
        return ghost;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void dispose() {
        alive = false;
    }
}
