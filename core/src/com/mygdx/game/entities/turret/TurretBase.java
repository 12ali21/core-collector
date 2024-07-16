package com.mygdx.game.entities.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.Bounds;
import com.mygdx.game.entities.World;
import com.mygdx.game.entities.Entity;

public class TurretBase extends Entity {

    public TurretBase(World world, Vector2 position) {
        super(world, new Bounds((int) position.x, (int) position.y, 2, 2));
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