package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Entity implements Drawable, Updatable{
    private final AssetManager assets;
    private final Batch batch;

    public Entity(AssetManager assets, Batch batch) {
        this.assets = assets;
        this.batch = batch;
    }
}
