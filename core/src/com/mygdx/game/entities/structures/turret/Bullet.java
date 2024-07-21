package com.mygdx.game.entities.structures.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.entities.EntityObj;
import com.mygdx.game.world.Game;

public class Bullet extends EntityObj {
    private static final float SCALE = 0.01f;

    private float lifetime = 2f;

    private final Body body;

    public Bullet(Game game, Vector2 position, float direction, float speed) {
        super(game);

        Texture t = assets.get("sprites/bullet.png", Texture.class);
        sprite = new Sprite(t);
//        sprite.setSize(scale * t.getHeight(), scale * t.getWidth());
        sprite.setScale(SCALE);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x, position.y);
        sprite.setRotation(direction + 90);
        sprite.setColor(0, 0, 0, 1);

        double rotation = Math.toRadians(direction);

        body = makeBody(position, direction);
        Vector2 velocity = new Vector2(1, 0);
        velocity.rotateDeg(direction);
        body.setLinearVelocity(velocity.scl(speed));
    }

    private Body makeBody(Vector2 position, float direction) {
        final Body body;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.01f, 0.04f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);
        bodyDef.angle = (float) Math.toRadians(direction);
        bodyDef.bullet = true;

        body = game.getWorld().createBody(bodyDef);
        body.setLinearDamping(1f);
        body.setFixedRotation(true);
        body.setUserData(this);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 10f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        lifetime -= deltaTime;
        if (lifetime < 0) {
            kill();
        }
        sprite.setOriginBasedPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    public void kill() {
        super.kill();
        game.getWorld().destroyBody(body);
    }
}
