package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ai.agents.EnemyAgent;
import com.mygdx.game.utils.Bodies;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class RedCreep extends Enemy {

    private static final float ATTACKING_RANGE = 1;
    private static final float DAMAGE = 50;
    private static final float DAMAGE_COOLDOWN = 1f;

    public RedCreep(Game game, Vector2 position) {
        super(game, 100);
        Texture t = TextureAssets.get(TextureAssets.ENEMY_SMALL_TEXTURE);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setScale(0.8f);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);
        health.setOffset(new Vector2(0, -0.5f));

        this.body = makeBody(position);
        this.agent = new EnemyAgent(game, this, body, ATTACKING_RANGE, DAMAGE, DAMAGE_COOLDOWN);

        movementSpeed = 2;
    }

    private Body makeBody(Vector2 position) {
        final Body body;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 3f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0f;

        body = Bodies.createEllipse(game, bodyDef, 0.14f, 0.35f, 8, fixtureDef);
        body.setLinearDamping(1f);
        body.setAngularDamping(2f);
        body.setUserData(this);
        return body;
    }

    public void stagger(float time) {
        agent.stagger(time);
    }

    @Override
    public void render() {
        super.render();
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        agent.update(deltaTime);
        super.update(deltaTime);
    }

    @Override
    public void dispose() {
        super.dispose();
        game.getWorld().destroyBody(body);
    }
}
