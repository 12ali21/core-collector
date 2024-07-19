package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.world.MapNode;
import com.mygdx.game.world.World;

import java.util.Iterator;

public class RedCreep extends Enemy {

    private DefaultGraphPath<MapNode> path;
    private Iterator<MapNode> pathIterator;
    MapNode currentDest;

    public RedCreep(World world, Vector2 position) {
        super(world, position);
        Texture t = assets.get("sprites/enemy_small.png", Texture.class);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);

        movementSpeed = 2;
        path = world.findPath((int) position.x, (int) position.y, 18, 18);
        pathIterator = path.iterator();
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        if (currentDest == null && pathIterator.hasNext()) {
            currentDest = pathIterator.next();
        } else if(currentDest != null) {
            boolean res = moveTo(currentDest.x, currentDest.y, deltaTime);
            if (res && pathIterator.hasNext()) {
                currentDest = pathIterator.next();
            }
        }
        super.update(deltaTime);
    }
}
