package com.mygdx.game.entities.others;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.world.Game;

public abstract class EntityObj extends Entity {
    protected Sprite sprite;

    public EntityObj(Game game) {
        super(game);
    }

    @Override
    public abstract Vector2 getCenter();
}
