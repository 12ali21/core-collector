package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity implements Drawable, Updatable{
    private final AssetManager assets;
    protected final Batch batch;
    private final Vector2 position;

    public Entity(AssetManager assets, Batch batch, Vector2 position) {
        this.assets = assets;
        this.batch = batch;
        this.position = position;
    }
}
