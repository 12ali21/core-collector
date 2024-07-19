package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.EntityObj;
import com.mygdx.game.world.World;

public abstract class Enemy extends EntityObj {
    protected float movementSpeed;

    public Enemy(World world, Vector2 position) {
        super(world, position);
    }

    protected boolean moveTo(float x, float y, float delta) {
        Vector2 diff = new Vector2(x - position.x, y - position.y);
        if (diff.len() < movementSpeed * delta) {
            position.set(x, y);
        } else {
            diff.setLength(movementSpeed * delta);
            position.add(diff);
        }
        return (x == position.x && y == position.y);
    }

    @Override
    public void render() {

    }

    @Override
    public void update(float deltaTime) {
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);

    }
}
