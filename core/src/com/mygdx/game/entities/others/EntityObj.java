package com.mygdx.game.entities.others;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.world.Game;

public abstract class EntityObj extends Entity {
    protected Sprite sprite;
    protected Body body;

    public EntityObj(Game game) {
        super(game);
    }

    @Override
    public Vector2 getCenter() {
        return new Vector2(body.getPosition());
    }
}
