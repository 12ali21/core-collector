package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class TurretHead extends Entity {
    private final Sprite sprite;
    private final float rotationSpeed = 50f;
    private Vector2 target;

    public TurretHead(AssetManager assets, Batch batch, Vector2 position) {
        super(assets, batch, position);
        Texture t = assets.get("sprites/turret_head.png", Texture.class);

        sprite = new Sprite(t);
        sprite.setSize(2, 2);
        sprite.setOrigin(sprite.getWidth() / 2, 0.65f);
        sprite.setOriginBasedPosition(position.x, position.y);
//        sprite.rotate(90);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    public void setTarget(float x, float y) {
        if(target == null) {
            target = new Vector2(x, y);
        } else {
            target.set(x, y);
        }
    }

    private Vector2 getWorldOrigin() {
        return new Vector2(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY());
    }

    private float getRotation() {
        return sprite.getRotation() + 90;
    }
    @Override
    public void update(float deltaTime) {
        Vector2 position = getWorldOrigin();
        if (target != null) {
            Vector2 direction = target.cpy().sub(position);
            float angle = direction.angleDeg();
            float currentAngle = getRotation();
            float diff = (angle - currentAngle) % 360;
            if (diff > 180) {
                diff -= 360;
            } else if (diff < -180) {
                diff += 360;
            }
            float rotation = Math.min(rotationSpeed * deltaTime, Math.abs(diff)) * Math.signum(diff);
            sprite.rotate(rotation);
        }
    }
}
