package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.world.MapNode;
import com.mygdx.game.world.World;

import java.util.Iterator;

public class RedCreep extends Enemy {

    private DefaultGraphPath<MapNode> path;
    private Iterator<MapNode> pathIterator;
    private MapNode currentDest;
    private Structure target;

    public RedCreep(World world, Vector2 position) {
        super(world, position);
        Texture t = assets.get("sprites/enemy_small.png", Texture.class);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);

        movementSpeed = 2;
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        // find the closest structure and the path to it
        if (target == null) {
            Array<Structure> structures = world.map.getStructures();
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
                path = world.findPath((int) position.x, (int) position.y, (int) target.getCenter().x, (int) target.getCenter().y);
                pathIterator = path.iterator();
            }
        }

        if (currentDest == null) {
            if (pathIterator != null && pathIterator.hasNext()) {
                currentDest = pathIterator.next();
            }
        } else {
            boolean res = moveTo(currentDest.x, currentDest.y, deltaTime);
            if (res)
                currentDest = null;
        }
        super.update(deltaTime);
    }
}
