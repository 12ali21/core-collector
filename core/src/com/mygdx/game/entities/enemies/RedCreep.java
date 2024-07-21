package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Debug;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.MapNode;

import java.util.Iterator;

public class RedCreep extends Enemy {

    private final float attackingRange = 1;
    private final float damage = 50;
    private final float DAMAGE_COOLDOWN = 1f;
    private final StateMachine<RedCreep, RedCreepState> stateMachine;
    private float timer = 0f;
    private DefaultGraphPath<MapNode> path;
    private Iterator<MapNode> pathIterator;
    private MapNode currentDest;
    private Structure target;

    public RedCreep(Game game, Vector2 position) {
        super(game);
        Texture t = assets.get("sprites/enemy_small.png", Texture.class);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);

        body = makeBody(position);

        stateMachine = new DefaultStateMachine<>(this, RedCreepState.IDLE);

        movementSpeed = 2;
    }

    private Body makeBody(Vector2 position) {
        final Body body;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.4f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        body = game.getWorld().createBody(bodyDef);
        body.setLinearDamping(1f);
        body.setFixedRotation(true);
        body.setUserData(this);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    public void setTarget(Structure target) {
        Vector2 position = body.getPosition();
        this.target = target;
        path = game.findPath((int) position.x, (int) position.y, (int) target.getCenter().x, (int) target.getCenter().y);
        pathIterator = path.iterator();
    }

    private void rotateTo(float angle) {
        if (Math.abs(angle - body.getAngle()) > 0.05f) {
            // rotate towards the target (clockwise or counter-clockwise depending on the shortest path
            float rotationSpeed = 5 * Math.signum(angle - body.getAngle());
            body.setAngularVelocity(rotationSpeed);
        } else {
            body.setAngularVelocity(0f);
        }
    }

    private boolean moveTo(float x, float y) {
        Debug.log("Moving to", x + ", " + y);
        Debug.log("Current position", body.getPosition());
        Vector2 position = body.getPosition();
        Vector2 target = new Vector2(x, y);
        Vector2 direction = target.cpy().sub(position).nor();
        body.setLinearVelocity(direction.scl(movementSpeed));
        rotateTo(direction.angleRad() + (float) Math.PI / 2);
        return position.dst(target) < 0.1f;
    }

    private void moveTowardsTarget() {
        if (currentDest == null) {
            if (pathIterator.hasNext()) {
                currentDest = pathIterator.next();
            }
        } else {
            // move to the center of the destination tile
            boolean res = moveTo(currentDest.x + 0.5f, currentDest.y + 0.5f);
            if (res)
                currentDest = null;
        }
    }

    private void stopMoving() {
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
    }

    private boolean targetWithinRange() {
        Vector2 position = body.getPosition();
        Debug.log("enemy position", "" + position);
        Debug.log("turret center", "" + target.getCenter());
        float dist = target.getCenter().dst(position);
        return dist < attackingRange;
    }

    private void attackTarget() {
        float delta = Gdx.graphics.getDeltaTime();
        timer -= delta;
        if (timer < 0) {
            target.getHealth().damage(damage);
            timer = DAMAGE_COOLDOWN;
        }
    }

    @Override
    public void update(float deltaTime) {
        stateMachine.update();
        Debug.log("Enemy state", "" + stateMachine.getCurrentState());
        super.update(deltaTime);
    }

    /**
     * ------------- STATES -------------
     */
    private enum RedCreepState implements State<RedCreep> {
        // wander around and look for targets
        IDLE() {
            @Override
            public void update(RedCreep entity) {
                // find the closest structure and the path to it
                Array<Structure> structures = entity.getWorld().map.getStructures();
                Vector2 position = entity.body.getPosition();

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
            public void enter(RedCreep entity) {
                if (entity.pathIterator.hasNext())
                    entity.currentDest = entity.pathIterator.next();
                else if (entity.target.isAlive())
                    entity.stateMachine.changeState(ATTACKING);
            }

            @Override
            public void update(RedCreep entity) {
                // if the target dies while moving, go back to idle
                if (entity.target.isAlive()) {
                    entity.moveTowardsTarget();
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
                }

                // if reached the attacking range, go to attacking
                if (entity.targetWithinRange()) {
                    entity.currentDest = null;
                    entity.stateMachine.changeState(ATTACKING);
                }
            }

            @Override
            public void exit(RedCreep entity) {
                entity.stopMoving();
            }
        },

        ATTACKING() {
            @Override
            public void update(RedCreep entity) {
                if (entity.target.isAlive()) {
                    entity.attackTarget();
                } else {
                    entity.target = null;
                    entity.stateMachine.changeState(IDLE);
                }
            }
        };

        @Override
        public void enter(RedCreep entity) {

        }

        @Override
        public void update(RedCreep entity) {

        }

        @Override
        public void exit(RedCreep entity) {

        }

        // TODO: Handle incoming damage
        @Override
        public boolean onMessage(RedCreep entity, Telegram telegram) {
            return false;
        }
    }

}
