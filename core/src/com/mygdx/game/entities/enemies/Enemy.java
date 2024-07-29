package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ai.EnemyAgent;
import com.mygdx.game.entities.others.EntityObj;
import com.mygdx.game.entities.others.HealthPoints;
import com.mygdx.game.world.Game;

public abstract class Enemy extends EntityObj {
    protected float movementSpeed;
    protected HealthPoints health;
    protected EnemyAgent agent;

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
        sprite.setPosition(body.getPosition().x - 0.5f, body.getPosition().y - 0.5f);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
        Vector2 pos = getCenter();
        health.setPosition(pos);
    }

    public EnemyAgent getAgent() {
        return agent;
    }

    public void damage(float amount) {
        health.damage(amount);
    }
}
