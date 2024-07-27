package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.ai.LocationUtils;
import com.mygdx.game.utils.Debug;

public class FormationAnchor extends SteerableAdapter<Vector2> implements Updatable, Drawable {
    private final Vector2 position = new Vector2();
    private final Vector2 linearVelocity = new Vector2();
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private final ReachOrientation<Vector2> reachOrientation;
    private final float maxAngularSpeed = 6;
    private final float maxAngularAcceleration = 3;
    private float maxLinearSpeed;
    private float maxLinearAcceleration;
    private SteeringBehavior<Vector2> steeringBehavior;
    private float orientation;

    public FormationAnchor() {
        reachOrientation = new ReachOrientation<>(this, new GameLocation())
                .setTimeToTarget(0.1f)
                .setDecelerationRadius(MathUtils.PI / 4f);
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

            steering.setZero();

            if (linearVelocity.len2() > getZeroLinearSpeedThreshold()) {
                reachOrientation.getTarget().setOrientation(calculateAngle());
            }

            reachOrientation.calculateSteering(steering);
            orientation += steering.angular * deltaTime;
        }
    }

    private float calculateAngle() {
        float angle = linearVelocity.angleRad();
        if (angle < 0) {
            angle += MathUtils.PI2;
        }
        angle = angle * 4 / MathUtils.PI;
        angle = Math.round(angle);
        angle = angle * MathUtils.PI / 4;
        return angle;
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.5f;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public float getOrientation() {
        return orientation;
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
        return LocationUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return LocationUtils.angleToVector(outVector, angle);
    }
}
