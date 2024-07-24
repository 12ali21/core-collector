package com.mygdx.game.entities.structures.turret;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.audio.SpatialSoundLooping;
import com.mygdx.game.audio.SpatialSoundNonLooping;
import com.mygdx.game.entities.Bullet;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapManager;

public class Turret extends Structure {
    private final StructurePart head;
    private final float rangeRadius;
    private final float rotationSpeed;
    private final float bulletSpeed;
    private final float bulletCooldown;
    private final Recoil recoil;
    private final StateMachine<Turret, TurretState> stateMachine;
    private final SpatialSoundNonLooping shootSound;
    private final SpatialSoundLooping rotateSound;
    private float headRotation = 0;
    private Enemy target;
    private float cooldownTimer = 0;

    protected Turret(Builder builder) {
        super(builder); // position is center, but bounds are bottom left
        this.head = builder.head;
        this.rangeRadius = builder.rangeRadius;
        this.rotationSpeed = builder.rotationSpeed;
        this.bulletSpeed = builder.bulletSpeed;
        this.bulletCooldown = builder.cooldown;
        head.sprite.setRotation(headRotation);
        recoil = new Recoil(
                builder.recoilImpulse,
                builder.recoilStoppingPower,
                builder.recoilMaxDistance,
                builder.recoilReturnVelocity);

        stateMachine = new DefaultStateMachine<>(this, TurretState.SEARCHING);
        health.setWidth(1.5f);
        health.setOffset(new Vector2(0, -1));

        shootSound = game.audio.newNonLoopingSpatialSoundEffect(AudioAssets.CANON_SHOOT);
        shootSound.setPosition(getCenter());

        rotateSound = game.audio.newLoopingSpatialSoundEffect(AudioAssets.TURRET_ROTATE);
        rotateSound.setPosition(getCenter());
        rotateSound.setVolume(0.3f);
    }

    /**
     * Checks if the target is in line of sight, and if not, forgets target
     */
    private void checkTargetSight() {
        Debug.drawPoint("turret center", getCenter());
        Debug.drawPoint("enemy center", target.getCenter());

        game.getWorld().rayCast((fixture, point, normal, fraction) -> {
            Object userData = fixture.getBody().getUserData();
            if (userData == null) return -1;
            if (userData.equals(MapManager.CellBodyType.WALL)) { // if there is a wall, there is no LOS
                target = null;
                return 0;
            }
            return -1;
        }, getCenter(), target.getCenter());
    }

    private void searchForTarget() {
        Vector2 center = getCenter();

        Enemy closest = null;
        float closestDist = Float.MAX_VALUE;

        Array<Enemy> enemies = game.getEnemies();
        for (Enemy enemy : enemies) {
            Vector2 enemyPos = enemy.getCenter();
            float distance = enemyPos.dst(center);
            // if the enemy is within range
            if (distance < closestDist && distance < rangeRadius) {
                final boolean[] inSight = {true};
                // check for line of sight
                game.getWorld().rayCast((fixture, point, normal, fraction) -> {
                    Object userData = fixture.getBody().getUserData();
                    if (userData.equals(MapManager.CellBodyType.WALL)) {
                        inSight[0] = false;
                        return 0; // no sight, stop ray
                    }
                    return -1;
                }, center, enemyPos);
                if (inSight[0]) {
                    closest = enemy;
                    closestDist = distance;
                }
            }
        }

        target = closest;
    }

    private boolean faceTarget(float delta) {
        final float targetEpsilon = 1f; // for target inaccuracy
        final float rotationEpsilon = 0.01f; // for floating point inaccuracy

        Vector2 targetPos = target.getCenter();

        Vector2 position = getCenter();
        Vector2 direction = targetPos.cpy().sub(position);
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

    private Vector2 getFiringPosition() {
        Vector2 offset = new Vector2(1.2f, 0f);
        offset.rotateDeg(headRotation);
        return getCenter().add(offset);
    }

    protected boolean fire(float delta) {
        cooldownTimer -= delta;
        if (cooldownTimer > 0) {
            return false;
        }
        Bullet bullet = new Bullet(game, getFiringPosition(), headRotation, bulletSpeed);
        shootSound.play();
        game.addEntity(bullet);
        cooldownTimer = bulletCooldown;

        recoil.fire();
        return true;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        Debug.log("Turret state: ", "" + stateMachine.getCurrentState());
        stateMachine.update();
        updateHead(deltaTime);
    }

    private void updateHead(float deltaTime) {
        float offset = recoil.updateOffset(deltaTime);
        Vector2 headPos = getCenter();
        Vector2 offsetVector = new Vector2(-1, 0).rotateDeg(headRotation).scl(offset);
        headPos.add(offsetVector);
        head.sprite.setOriginBasedPosition(headPos.x, headPos.y);
    }

    @Override
    public void dispose() {
        super.dispose();
        shootSound.dispose();
        rotateSound.dispose();
    }

    private enum TurretState implements State<Turret> {
        SEARCHING() {
            @Override
            public void update(Turret entity) {
                entity.searchForTarget();
                if (entity.target != null) {
                    entity.stateMachine.changeState(ATTACKING);
                }
            }
        },
        ATTACKING() {
            @Override
            public void update(Turret entity) {
                entity.checkTargetSight();
                if (entity.target == null) { // lost target
                    entity.stateMachine.changeState(SEARCHING);
                } else {
                    if (entity.target.isAlive()) {
                        if (entity.faceTarget(Gdx.graphics.getDeltaTime())) {
                            entity.rotateSound.pause();
                            entity.fire(Gdx.graphics.getDeltaTime());
                        } else {
                            entity.rotateSound.resume();
                        }
                    } else { // target died
                        entity.target = null;
                        entity.stateMachine.changeState(SEARCHING);
                    }
                }
            }

            @Override
            public void exit(Turret entity) {
                entity.rotateSound.stop();
            }
        };


        @Override
        public void enter(Turret entity) {

        }

        @Override
        public void update(Turret entity) {

        }

        @Override
        public void exit(Turret entity) {

        }

        @Override
        public boolean onMessage(Turret entity, Telegram telegram) {
            return false;
        }
    }

    public static class Builder extends Structure.Builder {

        private StructurePart head;
        private StructurePart base;
        private float rangeRadius = 10f;
        private float rotationSpeed = 50f;
        private float bulletSpeed = 30f;
        private float cooldown = 0.5f;
        private float recoilImpulse = 4f;
        private float recoilStoppingPower = 0.1f;
        private float recoilMaxDistance = 0.1f;
        private float recoilReturnVelocity = 0.2f;

        public Builder(Game game, float hitPoints) {
            super(game);
            this.width = 2;
            this.height = 2;
            this.maxHp = hitPoints;
        }

        public void setBase(StructurePart base) {
            this.base = base;
            base.setRenderPriority(1);
            addPart(base);
        }

        public void setHead(StructurePart head) {
            this.head = head;
            head.setRenderPriority(3);
            addPart(head);
        }

        public void setRangeRadius(float rangeRadius) {
            this.rangeRadius = rangeRadius;
        }

        public void setRotationSpeed(float rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
        }

        public void setBulletSpeed(float bulletSpeed) {
            this.bulletSpeed = bulletSpeed;
        }

        public void setFireRate(float rate) {
            this.cooldown = 60f / rate;
        }

        public void setRecoilImpulse(float impulse) {
            this.recoilImpulse = impulse;
        }

        public void setRecoilStoppingPower(float stoppingPower) {
            this.recoilStoppingPower = stoppingPower;
        }

        public void setRecoilMaxDistance(float maxDistance) {
            this.recoilMaxDistance = maxDistance;
        }

        public void setRecoilReturnVelocity(float returnVelocity) {
            this.recoilReturnVelocity = returnVelocity;
        }

        @Override
        public Turret build() {
            if (head == null || base == null) {
                throw new IllegalStateException("Can't build a turret without a base or head");
            }
            return new Turret(this);
        }
    }

    private static class Recoil {
        private final float impulse;
        private final float stoppingPower;
        private final float maxDistance;
        private final float returnVelocity;

        private float velocity;
        private float offset;

        public Recoil(float impulse, float stoppingPower, float maxDistance, float returnVelocity) {
            this.impulse = impulse;
            this.stoppingPower = stoppingPower;
            this.maxDistance = maxDistance;
            this.returnVelocity = returnVelocity;
        }

        public void fire() {
            velocity = impulse;
        }

        public float updateOffset(float delta) {
            final float epsilon = 0.01f;

            offset += velocity * delta;
            if (offset >= maxDistance) {
                offset = maxDistance;
                velocity = -returnVelocity;
            } else if (offset <= 0) {
                offset = 0;
                velocity = 0;
            } else if (velocity > 0) {
                velocity -= stoppingPower;
            } else if (velocity <= 0) {
                velocity = -returnVelocity;
            }

            if (Math.abs(offset) < epsilon) {
                offset = 0;
            }
            return offset;
        }
    }
}
