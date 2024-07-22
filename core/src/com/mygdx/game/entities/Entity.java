package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.world.Game;

public abstract class Entity implements Drawable, Updatable, Disposable {

    protected final AssetManager assets;
    protected final Batch batch;
    protected final Game game;
    protected boolean alive = true;

    private int renderPriority;

    public Entity(Game game) {
        this.game = game;
        this.assets = game.getAssets();
        this.batch = game.getBatch();
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

    public abstract Vector2 getCenter();

    public void kill() {
        alive = false;
    }

    @Override
    public void dispose() {

    }

    public Game getWorld() {
        return game;
    }
}
