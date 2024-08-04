package com.mygdx.game.entities.others;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Renderable;
import com.mygdx.game.Updatable;
import com.mygdx.game.world.Game;

public abstract class Entity implements Renderable, Updatable, Disposable {
    protected final Batch batch;
    protected final Game game;
    protected boolean alive = true;

    private int renderPriority;

    public Entity(Game game) {
        this.game = game;
        this.batch = game.getBatch();
    }

    public int getRenderPriority() {
        return renderPriority;
    }

    public void setRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
        game.entities.updateRenderingPriorities();
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
