package com.mygdx.game.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.limiters.LinearLimiter;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.ai.formation.FormationMembership;
import com.mygdx.game.entities.enemies.EnemiesManager;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.others.Bullet;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

import static com.mygdx.game.utils.Utils.convertToLinePath;

public class EnemyAgent extends Agent {
    private final StateMachine<EnemyAgent, EnemyState> stateMachine;
    private final FormationMembership membership;
    private final Enemy owner;
    private final float attackingRange;
    private final float damage;
    private final float damageCooldown;
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());
    private final LinearLimiter walkLimiter;
    private final LinearLimiter runLimiter;
    private final Arrive<Vector2> arriveTarget;
    private LinearLimiter currentLimiter;
    private Scheduler scheduler;
    private FollowPath<Vector2, LinePath.LinePathParam> followPathBehavior;
    private Structure target;
    private float timer = 0;
    private boolean staggered;

    public EnemyAgent(Game game, Enemy owner, Body body, float attackingRange, float damage, float damageCooldown) {
        super(game, body);
        this.owner = owner;
        this.attackingRange = attackingRange;
        this.damage = damage;
        this.damageCooldown = damageCooldown;
        stateMachine = new DefaultStateMachine<>(this, EnemyState.IDLE);
        membership = new FormationMembership(game, body, this);

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
                    .setTimeToTarget(0.1f)
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
        membership.update();
    }

    private void followPath() {
        followPathBehavior.calculateSteering(steeringOutput);
        applySteering(steeringOutput);
    }

    public void applySteering(SteeringAcceleration<Vector2> steering) {
        float angular = steering.angular;
        Vector2 linear = steering.linear;
        if (staggered)
            linear.scl(0.5f);
        body.setLinearVelocity(linear);
        body.setAngularVelocity(angular);
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
        arriveTarget.calculateSteering(steeringOutput);
        applySteering(steeringOutput);
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
    }

    public Telegraph getTelegraph() {
        return stateMachine;
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

        //TODO
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
                entity.arriveTarget.setTarget(new GameLocation(entity.target.getCenter()));
            }

            @Override
            public void update(EnemyAgent entity) {
                if (entity.target.isAlive()) {
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
        };

        @Override
        public void enter(EnemyAgent entity) {

        }

        @Override
        public void update(EnemyAgent entity) {
            // TODO: go back to land if out of border

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
                }
            }
            return false;
        }
    }
}
