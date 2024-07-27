package com.mygdx.game.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ai.formation.FormationMembership;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class EnemyAgent extends Agent {
    private final StateMachine<EnemyAgent, EnemyState> stateMachine;
    private final FormationMembership membership;
    private final float attackingRange;
    private final float damage;
    private final float damageCooldown;
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());
    float maxSpeedHolder = 0;
    Scheduler scheduler;
    private FollowPath<Vector2, LinePath.LinePathParam> followPathBehavior;
    private Structure target;
    private float timer = 0;
    private boolean staggered;

    public EnemyAgent(Game game, Body body, float attackingRange, float damage, float damageCooldown) {
        super(game, body);
        this.attackingRange = attackingRange;
        this.damage = damage;
        this.damageCooldown = damageCooldown;
        stateMachine = new DefaultStateMachine<>(this, EnemyState.IDLE);
        membership = new FormationMembership(game, body);
    }

    public void setTarget(Structure target) {
        GridPoint2 position = getGridPosition();
        this.target = target;
        DefaultGraphPath<MapNode> graphPath = game.map.findPath(
                position.x,
                position.y,
                (int) target.getCenter().x,
                (int) target.getCenter().y
        );
        LinePath<Vector2> path = convertToLinePath(graphPath);
        if (followPathBehavior == null) {
            followPathBehavior = new FollowPath<>(this, path, 0.5f)
                    .setDecelerationRadius(1f)
                    .setTimeToTarget(0.1f);
        } else {
            followPathBehavior.setPath(path);
        }
    }

    public void followPath() {
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
        float delta = Gdx.graphics.getDeltaTime();
        timer -= delta;
        if (timer < 0) {
            target.getHealth().damage(damage);
            timer = damageCooldown;
        }
    }

    public void stagger(float time) {
        staggered = true;
        scheduler = new Scheduler(() -> staggered = false, time, false, false);
        scheduler.start();
    }

    @Override
    public void update(float delta) {
        if (scheduler != null && scheduler.isRunning()) {
            scheduler.update(delta);
        }

        super.update(delta);
        stateMachine.update();
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
                Array<Structure> structures = entity.game.map.getStructures();
                Vector2 position = entity.getPosition();

                Structure target = null;
                float closest = Float.MAX_VALUE;
                for (Structure s : structures) {
                    Vector2 sPos = s.getCenter();
                    float dist = Vector2.dst(position.x, position.y, sPos.x, sPos.y);
                    if (dist < closest) {
                        closest = dist;
                        target = s;
                    }
                }
                if (target != null) {
                    entity.setTarget(target);
                    entity.stateMachine.changeState(MOVING);
                }
            }
        },

        // move towards the found target
        MOVING() {
            @Override
            public void update(EnemyAgent entity) {
                // if the target dies while moving, go back to idle
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

            @Override
            public void exit(EnemyAgent entity) {

            }
        },

        ATTACKING() {
            @Override
            public void update(EnemyAgent entity) {
                if (entity.target.isAlive()) {
                    entity.attackTarget();
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
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
            return false;
        }
    }
}
