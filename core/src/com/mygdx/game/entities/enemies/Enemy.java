package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.EntityObj;
import com.mygdx.game.world.World;

public abstract class Enemy extends EntityObj {
    protected final float width;
    protected final float height;
    protected float movementSpeed;
    protected final Polygon polygon;

    public Enemy(World world, Vector2 position, float width, float height) {
        super(world, position);
        this.width = width;
        this.height = height;
        setRenderPriority(2);
        float[] vertices = new float[] {
                position.x, position.y,
                position.x, position.y + height,
                position.x + width, position.y + height,
                position.x + width, position.y
        };
        polygon = new Polygon(vertices);
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

    public Polygon getPolygon() {
        return polygon;
    }

    @Override
    public void render() {

    }

    @Override
    public void update(float deltaTime) {
        sprite.setOriginBasedPosition(position.x, position.y);

    }
}
