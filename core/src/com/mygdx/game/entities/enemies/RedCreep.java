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
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Debug;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.world.MapNode;
import com.mygdx.game.world.World;

import java.util.Iterator;

public class RedCreep extends Enemy {

    private final float attackingRange = 1;
    private final float damage = 50;
    private final float DAMAGE_COOLDOWN = 1f;
    private float timer = 0f;

    private DefaultGraphPath<MapNode> path;
    private Iterator<MapNode> pathIterator;
    private MapNode currentDest;
    private Structure target;
    private final StateMachine<RedCreep, RedCreepState> stateMachine;

    public RedCreep(World world, Vector2 position) {
        super(world, position);
        Texture t = assets.get("sprites/enemy_small.png", Texture.class);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);

        stateMachine = new DefaultStateMachine<>(this, RedCreepState.IDLE);

        movementSpeed = 2;
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    public void setTarget(Structure target) {
        this.target = target;
        path = world.findPath((int) position.x, (int) position.y, (int) target.getCenter().x, (int) target.getCenter().y);
        pathIterator = path.iterator();
    }

    private void moveTowardsTarget() {
        if (currentDest == null) {
            if (pathIterator.hasNext()) {
                currentDest = pathIterator.next();
            }
        } else {
            // move to the center of the destination tile
            boolean res = moveTo(currentDest.x + 0.5f, currentDest.y+ 0.5f, Gdx.graphics.getDeltaTime());
            if (res)
                currentDest = null;
        }
    }

    private boolean targetWithinRange() {
        Debug.log("enemy position", "" + getPosition());
        Debug.log("turret center", "" + target.getCenter());
        float dist = target.getCenter().dst(this.getPosition());
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
