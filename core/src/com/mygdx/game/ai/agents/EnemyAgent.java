package com.mygdx.game.ai.agents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.limiters.LinearLimiter;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.ai.formation.FormationMembership;
import com.mygdx.game.entities.enemies.EnemiesManager;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.others.Bullet;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.*;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

import static com.mygdx.game.utils.Utils.convertToLinePath;

public class EnemyAgent extends Agent implements Disposable {
    private final StateMachine<EnemyAgent, EnemyState> stateMachine;
    private final FormationMembership membership;
    private final Enemy owner;
    private final float attackingRange;
    private final float damage;
    private final float damageCooldown;
    private final LinearLimiter walkLimiter;
    private final LinearLimiter runLimiter;
    private final Arrive<Vector2> arriveTarget;
    private LinearLimiter currentLimiter;
    private Scheduler scheduler;
    private FollowPath<Vector2, LinePath.LinePathParam> followPathBehavior;
    private Structure target;
    private float timer = 0;
    private boolean staggered;

    public EnemyAgent(Game game, Enemy owner, Vector2 position, float attackingRange, float damage, float damageCooldown) {
        super(game, Bodies.createCreepBody(game, position));
        this.owner = owner;
        this.attackingRange = attackingRange;
        this.damage = damage;
        this.damageCooldown = damageCooldown;
        stateMachine = new DefaultStateMachine<>(this, EnemyState.IDLE, EnemyState.GLOBAL);
        membership = new FormationMembership(game, this);

        setMaxLinearAcceleration(2f);
        setMaxLinearSpeed(5f);

        walkLimiter = new LinearLimiter(getMaxLinearAcceleration(), getMaxLinearSpeed());
        runLimiter = new LinearLimiter(getMaxLinearAcceleration() * 2,
                getMaxLinearSpeed() * 2);
        currentLimiter = walkLimiter;

        arriveTarget = new Arrive<>(this);
    }

    public boolean setTarget(Structure target) throws Utils.SingleNodePathException {
        GridPoint2 position = getGridPosition();
        DefaultGraphPath<MapNode> graphPath = game.map.findPath(
                position.x,
                position.y,
                (int) target.getCenter().x,
                (int) target.getCenter().y
        );
        if (graphPath == null) {
            return false;
        }
        LinePath<Vector2> path = convertToLinePath(graphPath);
        if (followPathBehavior == null) {
            followPathBehavior = new FollowPath<>(this, path, 0.5f)
                    .setDecelerationRadius(1f)
                    .setTimeToTarget(2f)
                    .setLimiter(currentLimiter);
        } else {
            followPathBehavior.setPath(path)
                    .setLimiter(currentLimiter);
        }
        this.target = target;
        return true;
    }

    public boolean canJoinFormation() {
        return stateMachine.getCurrentState().equals(EnemyState.IDLE) ||
                stateMachine.getCurrentState().equals(EnemyState.MOVING);
    }

    public FormationMembership getMembership() {
        return membership;
    }

    private void followFormation() {
        membership.calculateSteering(steering);
        applySteering();
    }

    private void followPath() {
        followPathBehavior.calculateSteering(steering);
        applySteering();
    }

    @Override
    public void applySteering() {
        if (staggered)
            steering.linear.scl(0.5f);
        super.applySteering();
    }

    private boolean targetWithinRange() {
        Vector2 position = body.getPosition();
        float dist = target.getCenter().dst(position);
        return dist < attackingRange;
    }

    private void attackTarget() {
        if (targetWithinRange()) {
            float delta = Gdx.graphics.getDeltaTime();
            timer -= delta;
            if (timer < 0) {
                target.getHealth().damage(damage);
                timer = damageCooldown;
            }
        }
        applyArrive();
    }

    private void applyArrive() {
        arriveTarget.calculateSteering(steering);
        applySteering();
    }

    public void stagger(float time) {
        staggered = true;
        scheduler = new Scheduler(() -> staggered = false, time, false, false);
        scheduler.start();
    }

    public void update(float delta) {
        if (scheduler != null && scheduler.isRunning()) {
            scheduler.update(delta);
        }

        super.update();
        stateMachine.update();
        Debug.log(this + " state", stateMachine.getCurrentState());
    }

    public Telegraph getTelegraph() {
        return stateMachine;
    }

    private Location<Vector2> getBorderLocation() {
        Location<Vector2> location = new GameLocation();
        Vector2 locVec = location.getPosition();
        locVec.set(getPosition());
        if (locVec.x < 0) locVec.x = 0;
        else if (locVec.x >= Constants.MAP_WIDTH) locVec.x = Constants.MAP_WIDTH - 1;
        if (locVec.y < 0) locVec.y = 0;
        else if (locVec.y >= Constants.MAP_HEIGHT) locVec.y = Constants.MAP_HEIGHT - 1;

        return location;
    }

    private void walk() {
        currentLimiter = walkLimiter;
        if (followPathBehavior != null) {
            followPathBehavior.setLimiter(currentLimiter);
        }
    }

    private void run() {
        currentLimiter = runLimiter;
        if (followPathBehavior != null) {
            followPathBehavior.setLimiter(currentLimiter);
        }
    }

    @Override
    public void dispose() {
        game.getWorld().destroyBody(body);
    }

    /*
     * ------------- STATES -------------
     */

    private enum EnemyState implements State<EnemyAgent> {
        // wander around and look for targets
        IDLE() {
            @Override
            public void update(EnemyAgent entity) {
                // find the closest structure and the path to it
                Structure target = EnemiesManager.getClosestStructure(entity.game, entity.getPosition());
                if (target != null) {
                    try {
                        if (entity.setTarget(target)) {
                            entity.stateMachine.changeState(MOVING);
                        }
                    } catch (Utils.SingleNodePathException e) { // already there
                        entity.stateMachine.changeState(ATTACKING);
                    }
                }
            }
        },

        // move towards the found target
        MOVING() {
            @Override
            public void enter(EnemyAgent entity) {
                entity.walk();
            }

            @Override
            public void update(EnemyAgent entity) {
                // if the target dies while moving, go back to idle
                if (entity.target != null && entity.target.isAlive()) {
                    if (entity.getPosition().dst(entity.target.getCenter()) < Constants.AGGRO_RANGE) {
                        entity.stateMachine.changeState(AGGRO);
                    } else {
                        entity.followPath();
                        // if reached the attacking range, go to attacking
                        if (entity.targetWithinRange()) {
                            entity.stateMachine.changeState(ATTACKING);
                        }
                    }
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
                }
            }
        },

        AGGRO() {
            @Override
            public void enter(EnemyAgent entity) {
                entity.run();
            }

            @Override
            public void update(EnemyAgent entity) {
                if (entity.target != null && entity.target.isAlive()) {
                    entity.followPath();
                    // if reached the attacking range, go to attacking
                    if (entity.targetWithinRange()) {
                        entity.stateMachine.changeState(ATTACKING);
                    }
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
                }
            }
        },

        ATTACKING() {
            @Override
            public void enter(EnemyAgent entity) {
                if (entity.target == null) {
                    entity.stateMachine.changeState(IDLE);
                } else {
                    entity.arriveTarget.setTarget(new GameLocation(entity.target.getCenter()));
                }
            }

            @Override
            public void update(EnemyAgent entity) {
                if (entity.target != null && entity.target.isAlive()) {
                    entity.attackTarget();
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
                }
            }
        },

        FOLLOW_FORMATION() {
            @Override
            public void update(EnemyAgent entity) {
                entity.followFormation();
            }
        },

        RETURN_TO_MAP() {
            @Override
            public void enter(EnemyAgent entity) {
                entity.arriveTarget.setTarget(entity.getBorderLocation());
            }

            @Override
            public void update(EnemyAgent entity) {
                if (entity.game.map.isWithinBoundary((int) entity.getPosition().x, (int) entity.getPosition().y)) {
                    entity.stateMachine.changeState(IDLE);
                } else {
                    entity.applyArrive();
                }
            }
        },

        GLOBAL() {
            @Override
            public void update(EnemyAgent entity) {
                if (!entity.game.map.isWithinBoundary((int) entity.getPosition().x, (int) entity.getPosition().y)) {
                    entity.stateMachine.changeState(RETURN_TO_MAP);
                }
            }
        };

        @Override
        public void enter(EnemyAgent entity) {

        }

        @Override
        public void update(EnemyAgent entity) {

        }

        @Override
        public void exit(EnemyAgent entity) {

        }

        @Override
        public boolean onMessage(EnemyAgent entity, Telegram telegram) {
            if (telegram.message == MessageType.JOIN_FORMATION.ordinal()) { // Joining Formation
                entity.stateMachine.changeState(FOLLOW_FORMATION);
            } else if (telegram.message == MessageType.BREAK_FORMATION.ordinal()) { // Breaking Formation
                try {
                    entity.setTarget((Structure) telegram.extraInfo);
                } catch (Utils.SingleNodePathException e) {
                    entity.stateMachine.changeState(ATTACKING);
                }
                entity.stateMachine.changeState(AGGRO);
            } else if (telegram.message == MessageType.DAMAGE.ordinal()) { // Taking Damage
                Bullet bullet = (Bullet) telegram.extraInfo;
                entity.stagger(0.3f);
                entity.owner.damage(bullet.getDamage());
                if (entity.stateMachine.getCurrentState() == FOLLOW_FORMATION) {
                    entity.membership.requestFormationBreak(bullet.getOwner());
                } else if (entity.stateMachine.getCurrentState() == MOVING) {
                    try {
                        entity.setTarget(bullet.getOwner());
                        entity.stateMachine.changeState(AGGRO);
                    } catch (Utils.SingleNodePathException e) {
                        entity.target = bullet.getOwner();
                        entity.stateMachine.changeState(ATTACKING);
                    }
                }
            }
            return false;
        }
    }
}
