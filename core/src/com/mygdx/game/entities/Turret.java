package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import org.w3c.dom.Text;

public class Turret extends Entity {
    private final Sprite turretBaseSprite;

    public Turret(AssetManager assets, Batch batch) {
        super(assets, batch);
        Texture t = assets.get("sprites/turret_base.png", Texture.class);
        turretBaseSprite = new Sprite(t);
    }

    @Override
    public void render() {

    }

    @Override
    public void update(float deltaTime) {

    }
}
