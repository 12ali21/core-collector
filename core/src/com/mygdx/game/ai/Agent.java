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
    private final Game game;
    private final Sprite sprite;
    private final LookWhereYouAreGoing<Vector2> look;

    private SteeringAcceleration<Vector2> angularSteering = new SteeringAcceleration<>(new Vector2());
    private boolean tagged;
    private float maxLinearSpeed = 2f;
    private float maxLinearAcc = 40f;
    private float maxAngularSpeed = 10;
    private float maxAngularAcc = 20;


    public Agent(Game game) {
        this.game = game;
        body = makeBody(new Vector2(1.5f, 1.5f));
        sprite = new Sprite(TextureAssets.get(TextureAssets.ENEMY_SMALL_TEXTURE));
        sprite.setSize(1, 1);
        sprite.setOriginCenter();

        look = new LookWhereYouAreGoing<>(this)
                .setTimeToTarget(0.5f)
                .setDecelerationRadius(MathUtils.PI / 2f);


    }

    private Body makeBody(Vector2 position) {
        final Body body;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.2f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        body = game.getWorld().createBody(bodyDef);
        body.setLinearDamping(1f);
        body.setAngularDamping(2f);
        body.setUserData(this);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    public void update(float delta) {
        Debug.log("Agent Speed", getLinearVelocity().len());
        Debug.drawPoint("agent pos" + this, getPosition());

        // Update orientation and angular velocity
        look.calculateSteering(angularSteering);
        body.applyTorque(angularSteering.angular, true);
        System.out.println(angularSteering.angular);
    }

    public void render() {
        sprite.setOriginBasedPosition(body.getPosition().x, body.getPosition().y);
        sprite.setRotation((body.getAngle() * MathUtils.radiansToDegrees) + 90);
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
        return (float) Math.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float) Math.sin(angle);
        outVector.y = (float) Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new GameLocation();
    }
}
