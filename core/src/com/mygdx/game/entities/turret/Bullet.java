package com.mygdx.game.entities.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.EntityObj;
import com.mygdx.game.entities.World;

public class Bullet extends EntityObj {
    private static final float SCALE = 0.01f;
    private final Vector2 velocity;

    private float lifetime = 2f;

    public Bullet(World world, Vector2 position, float direction, float speed) {
        super(world, position);

        Texture t = assets.get("sprites/bullet.png", Texture.class);
        sprite = new Sprite(t);
//        sprite.setSize(scale * t.getHeight(), scale * t.getWidth());
        sprite.setScale(SCALE);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x, position.y);
        sprite.setRotation(direction + 90);

        double rotation = Math.toRadians(direction);
        velocity = new Vector2((float) Math.cos(rotation) * speed, (float) Math.sin(rotation) * speed);

    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        lifetime -= deltaTime;
        if (lifetime < 0) {
            dispose();
        }
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        sprite.setOriginBasedPosition(position.x, position.y);
    }
}
