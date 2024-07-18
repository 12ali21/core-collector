package com.mygdx.game.entities.turret;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.Structure;
import com.mygdx.game.entities.World;

public class Turret extends Structure {
    private final StructurePart base;
    private final StructurePart head;
    private final float rotationSpeed;
    private final float bulletSpeed;
    private final float bulletCooldown;
    private float headRotation = 0;
    private Vector2 target;
    private float cooldownTimer = 0;

    private Turret(Builder builder) {
        super(builder); // position is center, but bounds are bottom left
        this.base = builder.base;
        this.head = builder.head;
        this.rotationSpeed = builder.rotationSpeed;
        this.bulletSpeed = builder.bulletSpeed;
        this.bulletCooldown = builder.cooldown;
        head.sprite.setRotation(headRotation);
    }

    public void setTarget(float x, float y) {
        if (target == null) {
            target = new Vector2(x, y);
        } else {
            target.set(x, y);
        }
    }

    private Vector2 getCenter() {
        return new Vector2(bounds.x + (float) bounds.width / 2, bounds.y + (float) bounds.height / 2);
    }

    private boolean findTarget(float delta) {
        final float targetEpsilon = 1f; // for target inaccuracy
        final float rotationEpsilon = 0.01f; // for floating point inaccuracy

        Vector2 position = getCenter();
        if (target != null) {
            Vector2 direction = target.cpy().sub(position);
            float angle = direction.angleDeg();
            float diff = (angle - headRotation) % 360;
            if (diff > 180) {
                diff -= 360;
            } else if (diff < -180) {
                diff += 360;
            }

            if (Math.abs(diff) > rotationEpsilon) {
                float rotation = Math.min(rotationSpeed * delta, Math.abs(diff)) * Math.signum(diff);
                head.sprite.rotate(rotation);
                headRotation = head.sprite.getRotation();
            }

            // finding target
            return Math.abs(diff) < targetEpsilon; // on target
        }
        return false; // no target
    }

    private Vector2 getFiringPosition() {
        Vector2 offset = new Vector2(1.2f, 0f);
        offset.rotateDeg(headRotation);
        return getCenter().add(offset);
    }

    private void fire(float delta) {
        cooldownTimer -= delta;
        if (cooldownTimer > 0) {
            return;
        }
        Bullet bullet = new Bullet(world, getFiringPosition(), headRotation, bulletSpeed);
        world.addEntity(bullet);
        cooldownTimer = bulletCooldown;
    }

    @Override
    public void update(float deltaTime) {
        boolean onTarget = findTarget(deltaTime);

        if (onTarget) {
            // shoot target
            fire(deltaTime);
        }
    }

    public static class Builder extends Structure.Builder {

        private StructurePart base;
        private StructurePart head;
        private float rotationSpeed = 50f;
        private float bulletSpeed = 30f;
        private float cooldown = 0.5f;

        public Builder(World world) {
            super(world);
        }

        public Builder setBase(StructurePart base) {
            this.base = base;
            base.setRenderPriority(1);
            addPart(base);
            return this;
        }

        public Builder setHead(StructurePart head) {
            this.head = head;
            head.setRenderPriority(3);
            addPart(head);
            return this;
        }

        public Builder setRotationSpeed(float rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
            return this;
        }

        public Builder setBulletSpeed(float bulletSpeed) {
            this.bulletSpeed = bulletSpeed;
            return this;
        }

        public Builder setFireRate(float rate) {
            this.cooldown = 60f / rate;
            return this;
        }

        public Builder setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height); // set bounds (bottom left corner
            return this;
        }

        @Override
        public Turret build() {
            return new Turret(this);
        }
    }
}
