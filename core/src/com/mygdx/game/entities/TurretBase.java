package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class TurretBase extends Entity {
    private final Sprite sprite;

    public TurretBase(AssetManager assets, Batch batch, Vector2 position) {
        super(assets, batch, position);
        Texture t = assets.get("sprites/turret_base.png", Texture.class);

        sprite = new Sprite(t);
        sprite.setSize(2f, 2f);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        sprite.setOriginBasedPosition(position.x, position.y);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {

    }
}
