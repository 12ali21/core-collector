package com.mygdx.game.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.world.Game;

public class Bodies {
    public static Body createEllipse(Game game, BodyDef bodyDef, float rX, float rY, int segments, FixtureDef fixtureDef) {
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
}
