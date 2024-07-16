package com.mygdx.game.entities.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.Bounds;
import com.mygdx.game.entities.World;
import com.mygdx.game.entities.Entity;

public class Turret extends Entity {

    private static class MultiSprite extends Sprite{
        private final Sprite base;
        private final Sprite head;

        public MultiSprite(Sprite base, Sprite head) {
            this.base = base;
            this.head = head;
        }

        @Override
        public void draw(Batch batch) {
            base.draw(batch);
            head.draw(batch);
        }

        @Override
        public void setOriginBasedPosition(float x, float y) {
            base.setOriginBasedPosition(x, y);
            head.setOriginBasedPosition(x, y);
        }
    }


    private final TurretBase base;
    private final float rotationSpeed = 50f;
    private Vector2 target;

    private final float bulletSpeed = 30f;
    private final float bulletCooldown = 0.5f;
    private float cooldownTimer = 0;

    public Turret(World world, Vector2 position, TurretBase base) {
        super(world, new Bounds((int) position.x, (int) position.y, 2, 2));
        this.base = base;
        Texture t = assets.get("sprites/turret_head.png", Texture.class);

        sprite = new Sprite(t);
        sprite.setSize(2, 2);
        sprite.setOrigin(sprite.getWidth() / 2, 0.65f);
        sprite.setOriginBasedPosition(position.x, position.y);
    }

    @Override
    public Sprite getGhost() {
        return new MultiSprite(base.getGhost(), super.getGhost());
    }

    public void setTarget(float x, float y) {
        if (target == null) {
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

    private boolean findTarget(float delta) {
        final float targetEpsilon = 1f;
        final float rotationEpsilon = 0.01f;

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

            if (Math.abs(diff) > rotationEpsilon) {
                float rotation = Math.min(rotationSpeed * delta, Math.abs(diff)) * Math.signum(diff);
                sprite.rotate(rotation);
            }

            // finding target
            return Math.abs(diff) < targetEpsilon; // on target
        }
        return false; // no target
    }

    private Vector2 getFiringPosition() {
        Vector2 offset = new Vector2(0, 1.2f);
        offset.rotateDeg(sprite.getRotation());
        return getWorldOrigin().add(offset);
    }

    private void fire(float delta) {
        cooldownTimer -= delta;
        if (cooldownTimer > 0) {
            return;
        }
        Bullet bullet = new Bullet(world, getFiringPosition(), getRotation(), bulletSpeed);
        bullet.addToWorld(false);
        cooldownTimer = bulletCooldown;
    }

    @Override
    public void render() {
        base.render();
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        boolean onTarget = findTarget(deltaTime);

        if (onTarget) {
            // shoot target
            fire(deltaTime);
        }
    }
}
