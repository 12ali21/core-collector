package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.world.World;

public abstract class Entity implements Drawable, Updatable, Disposable {

    protected final AssetManager assets;
    protected final Batch batch;
    protected final World world;
    protected boolean alive = true;

    private int renderPriority;

    public Entity(World world) {
        this.world = world;
        this.assets = world.getAssets();
        this.batch = world.getBatch();
    }

    public void setRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
    }

    public int getRenderPriority() {
        return renderPriority;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public void dispose() {
        alive = false;
    }

    public World getWorld() {
        return world;
    }
}
