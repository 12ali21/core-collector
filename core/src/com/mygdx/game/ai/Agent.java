package com.mygdx.game.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;


public class Agent implements Steerable<Vector2> {
    protected final Body body;
    protected final Game game;
    private final Sprite sprite;
    private final LookWhereYouAreGoing<Vector2> look;

    private SteeringAcceleration<Vector2> angularSteering = new SteeringAcceleration<>(new Vector2());
    private boolean tagged;
    private float maxLinearSpeed = 5f;
    private float maxLinearAcc = 2f;
    private float maxAngularSpeed = 10;
    private float maxAngularAcc = 20;


    public Agent(Game game) {
        this.game = game;
        body = makeBody(new Vector2(1.5f, 1.5f));
        sprite = new Sprite(TextureAssets.get(TextureAssets.ENEMY_SMALL_TEXTURE));
        sprite.setSize(1, 1);
        sprite.setOriginCenter();

        look = new LookWhereYouAreGoing<>(this).setTimeToTarget(0.5f).setDecelerationRadius(MathUtils.PI / 2f);


    }

    private Body createEllipse(BodyDef bodyDef, float rX, float rY, int segments, FixtureDef upperHalfDef, FixtureDef lowerHalfDef) {
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
        upperHalfDef.shape = shape;
        body.createFixture(upperHalfDef).setUserData(this);
        for (int i = 0; i < segments - 2; i++) {
            float angle = (float) i / (segments - 3) * MathUtils.PI;
            vertices[i] = new Vector2(MathUtils.cos(angle) * rX, -MathUtils.sin(angle) * rX - (rY - rX));
        }
        vertices[segments - 2] = new Vector2(rX, 0);
        vertices[segments - 1] = new Vector2(-rX, 0);

        shape.set(vertices);
        lowerHalfDef.shape = shape;
        body.createFixture(lowerHalfDef).setUserData(this);
        return body;
    }

    private Body makeBody(Vector2 position) {
        final Body body;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        FixtureDef upperHalfDef = new FixtureDef();
        upperHalfDef.density = 1.5f;
        upperHalfDef.friction = 0.2f;
        upperHalfDef.restitution = 0f;

        FixtureDef lowerHalfDef = new FixtureDef();
        lowerHalfDef.density = 1.5f;
        lowerHalfDef.friction = 0.2f;
        lowerHalfDef.restitution = 0f;

        body = createEllipse(bodyDef, 0.2f, 0.4f, 8, upperHalfDef, lowerHalfDef);
        body.setLinearDamping(1f);
        body.setAngularDamping(2f);
        return body;
    }

    public void update(float delta) {
        Debug.log("Agent Speed", getLinearVelocity().len());
        Debug.drawPoint("agent pos" + this, getPosition());

        // Update orientation and angular velocity
        look.calculateSteering(angularSteering);
        body.applyTorque(angularSteering.angular, true);
    }

    public void render() {
        sprite.setOriginBasedPosition(body.getPosition().x, body.getPosition().y);
        sprite.setRotation((body.getAngle() * MathUtils.radiansToDegrees));
        sprite.draw(game.getBatch());

    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    // The radius of the circle that encloses this agent
    @Override
    public float getBoundingRadius() {
        return 0.5f;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.5f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {

    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcc;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcc = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcc;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcc = maxAngularAcceleration;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle() - MathUtils.HALF_PI;
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return LocationUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return LocationUtils.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation() {
        return new GameLocation();
    }
}
