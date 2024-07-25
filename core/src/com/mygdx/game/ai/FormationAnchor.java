package com.mygdx.game.ai;

import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Debug;

public class FormationAnchor extends SteerableAdapter<Vector2> implements Updatable, Drawable {
    private final Vector2 position = new Vector2();
    private final Vector2 linearVelocity = new Vector2();
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private float maxLinearSpeed;
    private float maxLinearAcceleration;
    private SteeringBehavior<Vector2> steeringBehavior;

    public FormationAnchor() {

    }

    @Override
    public void render() {

    }

    @Override
    public void update(float deltaTime) {
        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steering);
            Debug.drawPoint(this + "pos", position);
            position.mulAdd(linearVelocity, deltaTime);
            linearVelocity.mulAdd(steering.linear, deltaTime).limit(maxLinearSpeed);
        }
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
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
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return linearVelocity;
    }

    @Override
    public float getBoundingRadius() {
        return 1;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new GameLocation();
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return super.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return super.angleToVector(outVector, angle);
    }
}
