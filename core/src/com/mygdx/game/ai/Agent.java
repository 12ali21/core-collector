package com.mygdx.game.ai;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
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
    private final Game game;
    private final Body body;
    SteeringBehavior<Vector2> steeringBehavior;
    Sprite sprite;
    private SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private boolean tagged;
    private float maxLinearSpeed = 2f;
    private float maxLinearAcc = 25f;
    private float maxAngularSpeed;
    private float maxAngularAcc;

    public Agent(Game game) {
        this.game = game;
        body = makeBody(new Vector2(1.5f, 1.5f));
        sprite = new Sprite(TextureAssets.get(TextureAssets.ENEMY_SMALL_TEXTURE));
        sprite.setSize(1, 1);
        sprite.setOriginCenter();
//        DefaultGraphPath<MapNode> testGraphPath = game.map.findPath(1, 1, 25, 15);
//        Array<Vector2> vertices = new Array<>();
//        for (MapNode node : testGraphPath) {
//            vertices.add(new Vector2(node.x + .5f, node.y + .5f));
//        }
//        LinePath<Vector2> path = new LinePath<>(vertices, true);
//
//        followPath = new FollowPath<>(this, path, 1f);
//        followPath.setPredictionTime(0.1f);
//        followPath.setDecelerationRadius(1f);
//        followPath.setArrivalTolerance(0.1f);
    }

    private Body makeBody(Vector2 position) {
        final Body body;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.4f);
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
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef).setUserData(this);
        return body;
    }

    public void update(float delta) {
        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steering);
            Debug.drawPoint("agent pos" + this, getPosition());
            applySteering(steering, delta);
        }

        // Update orientation and angular velocity
        body.applyTorque(steering.angular, true);

//        else {
//            // If we haven't got any velocity, then we can do nothing.
//            if (!linearVelocity.isZero(getZeroLinearSpeedThreshold())) {
//                float newOrientation = vectorToAngle(linearVelocity);
//                angularVelocity = (newOrientation - getRotation() * MathUtils.degreesToRadians) * delta; // this is superfluous if independentFacing is always true
//                setRotation(newOrientation * MathUtils.radiansToDegrees);
//            }
//        }
    }

    public void render() {
        sprite.setOriginBasedPosition(body.getPosition().x, body.getPosition().y);
        sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
        sprite.draw(game.getBatch());
    }

    protected void applySteering(SteeringAcceleration<Vector2> steering, float deltaTime) {

        // Update position and linear velocity.
        if (!steering.linear.isZero()) {
            // this method internally scales the force by deltaTime
            body.applyForceToCenter(steering.linear, true);
        }


        // If we haven't got any velocity, then we can do nothing.
//        Vector2 linVel = getLinearVelocity();
//        if (!linVel.isZero(getZeroLinearSpeedThreshold())) {
//            float newOrientation = vectorToAngle(linVel);
//            body.setAngularVelocity((newOrientation - getAngularVelocity()) * deltaTime); // this is superfluous if independentFacing is always true
//            body.setTransform(body.getPosition(), newOrientation);
//        }
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return 1;
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
        return 0.001f;
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
