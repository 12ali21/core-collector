package com.mygdx.game.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.world.Game;

public class Bodies {
    private static Body createEllipse(Game game, BodyDef bodyDef, float rX, float rY, int segments, FixtureDef fixtureDef) {
        if (segments <= 4) {
            throw new IllegalArgumentException("can't make an ellipse with " + segments + " segments");
        }
        Body body = game.getWorld().createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[segments];
        for (int i = 0; i < segments - 2; i++) {
            float angle = (float) i / (segments - 3) * MathUtils.PI;
            vertices[i] = new Vector2(MathUtils.cos(angle) * rX, MathUtils.sin(angle) * rX + (rY - rX));
        }
        vertices[segments - 2] = new Vector2(rX, 0);
        vertices[segments - 1] = new Vector2(-rX, 0);

        shape.set(vertices);
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        for (Vector2 vertex : vertices) {
            vertex.y = -vertex.y;
        }
        shape.set(vertices);
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        shape.dispose();
        return body;
    }

    private static BodyDef getTemplateBodyDef(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);
        return bodyDef;
    }

    private static FixtureDef getTemplateFixtureDef() {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0f;
        return fixtureDef;
    }

    public static Body createCreepBody(Game game, Vector2 position) {
        final Body body;

        body = Bodies.createEllipse(game, getTemplateBodyDef(position), 0.14f, 0.35f, 8, getTemplateFixtureDef());
        body.setLinearDamping(1f);
        body.setAngularDamping(2f);

        return body;
    }

    public static Body createBotBody(Game game, Vector2 position) {
        final Body body;
        BodyDef bodyDef = getTemplateBodyDef(position);

        FixtureDef fixtureDef = getTemplateFixtureDef();
        fixtureDef.isSensor = true; // because it flies

        CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        fixtureDef.shape = shape;

        body = game.getWorld().createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setLinearDamping(1f);
        body.setAngularDamping(2f);
        return body;

    }
}
