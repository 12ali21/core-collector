package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entities.EntityObj;
import com.mygdx.game.entities.HealthPoints;
import com.mygdx.game.world.Game;

public abstract class Enemy extends EntityObj {
    protected float movementSpeed;
    protected HealthPoints health;

    public Enemy(Game game, float maxHp) {
        super(game);
        health = new HealthPoints(game, maxHp, this::kill);
        setRenderPriority(2);
    }


    @Override
    public void render() {
        health.render();
    }

    public HealthPoints getHealth() {
        return health;
    }

    @Override
    public void update(float deltaTime) {
        Vector2 pos = getCenter();
        sprite.setOriginBasedPosition(pos.x, pos.y);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
        health.setPosition(pos);
    }
}
