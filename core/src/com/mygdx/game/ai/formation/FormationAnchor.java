package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.behaviors.ReachOrientation;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Updatable;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.ai.LocationUtils;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class FormationAnchor extends SteerableAdapter<Vector2> implements Updatable {
    private final Game game;
    private final FormationManager owner;
    private final Vector2 position = new Vector2();
    private final Vector2 linearVelocity = new Vector2();
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private final ReachOrientation<Vector2> reachOrientation;
    private final Vector2 target;
    private FollowPath<Vector2, LinePath.LinePathParam> followPath;
    private float maxLinearSpeed = 2f;
    private float maxLinearAcceleration = 2f;
    private float orientation;

    public FormationAnchor(Game game, FormationManager owner, Vector2 start, GridPoint2 target) {
        this.game = game;
        this.owner = owner;
        this.target = new Vector2(target.x, target.y);
        reachOrientation = new ReachOrientation<>(this, new GameLocation())
                .setTimeToTarget(0.1f)
                .setDecelerationRadius(MathUtils.PI / 4f);
        this.position.set(start);
    }

    public boolean makePath() {
        DefaultGraphPath<MapNode> graphPath = game.map.findPath(
                (int) position.x, (int) position.y, (int) target.x, (int) target.y);
        LinePath<Vector2> path;
        try {
            path = Utils.convertToLinePath(graphPath);
            followPath = new FollowPath<>(this, path, 3f);
            followPath.setPredictionTime(0);
            followPath.setDecelerationRadius(.5f);
            return true;
        } catch (Utils.SingleNodePathException e) {
            return false;
        }
    }

    @Override
    public void update(float deltaTime) {
        followPath.calculateSteering(steering);
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

    public float distanceToTarget() {
        return getPosition().dst(target);
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.5f;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return 3f;
    }

    @Override
    public float getMaxAngularSpeed() {
        return 6f;
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
