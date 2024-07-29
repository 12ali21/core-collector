package com.mygdx.game.entities.others;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapManager;

public class Bullet extends EntityObj {
    private static final float SCALE = 0.01f;
    private final Body body;
    private final float damage = 10f;
    private final float lifetime = 2f;
    private final Scheduler destroyScheduler;
    private final Structure owner;

    public Bullet(Game game, Structure owner, float direction, float speed, Vector2 position) {
        super(game);
        this.owner = owner;

        Texture t = TextureAssets.get(TextureAssets.BULLET_TEXTURE);
        sprite = new Sprite(t);
//        sprite.setSize(scale * t.getHeight(), scale * t.getWidth());
        sprite.setScale(SCALE);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x, position.y);
        sprite.setRotation(direction + 90);
        sprite.setColor(0, 0, 0, 1);

        body = makeBody(position, direction);
        Vector2 velocity = new Vector2(1, 0);
        velocity.rotateDeg(direction);
        body.setLinearVelocity(velocity.scl(speed));
        destroyScheduler = new Scheduler(this::kill, lifetime);
        destroyScheduler.start();
    }

    public static boolean handleContact(Contact contact) {
        Object userDataA = contact.getFixtureA().getBody().getUserData();
        Object userDataB = contact.getFixtureB().getBody().getUserData();
        if (userDataA == null || userDataB == null) {
            return false;
        }

        Bullet bullet = null;
        Enemy enemy = null;
        if (userDataA instanceof Bullet) {
            bullet = (Bullet) userDataA;
            if (userDataB instanceof Enemy) {
                enemy = (Enemy) userDataB;
            } else if (userDataB.equals(MapManager.CellBodyType.WALL)) { //collision with wall
                bullet.kill();
                return true;
            }
        } else if (userDataB instanceof Bullet) {
            bullet = (Bullet) userDataB;
            if (userDataA instanceof Enemy) {
                enemy = (Enemy) userDataA;
            } else if (userDataA.equals(MapManager.CellBodyType.WALL)) { //collision with wall
                bullet.kill();
                return true;
            }
        }
        if (bullet != null && enemy != null) {
            MessageManager.getInstance().dispatchMessage(
                    null,
                    enemy.getAgent().getTelegraph(),
                    MessageType.DAMAGE.ordinal(),
                    bullet
            );
            bullet.kill();
            return true;
        }
        return false;
    }

    private Body makeBody(Vector2 position, float direction) {
        final Body body;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.06f, 0.01f);
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
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        sprite.setOriginBasedPosition(body.getPosition().x, body.getPosition().y);
        destroyScheduler.update(deltaTime);
    }

    public float getDamage() {
        return damage;
    }

    public Structure getOwner() {
        return owner;
    }

    @Override
    public void dispose() {
        super.dispose();
        game.getWorld().destroyBody(body);
    }
}
