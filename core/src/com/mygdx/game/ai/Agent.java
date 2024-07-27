package com.mygdx.game.ai;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;


public class Agent implements Steerable<Vector2>, Updatable {
    protected final Body body;
    protected final Game game;
    private final LookWhereYouAreGoing<Vector2> look;
    private final GridPoint2 gridPos = new GridPoint2();

    private final SteeringAcceleration<Vector2> angularSteering = new SteeringAcceleration<>(new Vector2());
    private boolean tagged;
    private float maxLinearSpeed = 5f;
    private float maxLinearAcc = 2f;
    private float maxAngularSpeed = 10;
    private float maxAngularAcc = 10f;


    public Agent(Game game, Body body) {
        this.game = game;
        this.body = body;

        look = new LookWhereYouAreGoing<>(this).setTimeToTarget(1f)
                .setDecelerationRadius(MathUtils.PI / 4f);
    }

    @Override
    public void update(float delta) {
        Debug.log("Agent Speed", getLinearVelocity().len());
        Debug.drawPoint("agent pos" + this, getPosition());

        // Update orientation and angular velocity
        look.calculateSteering(angularSteering);
        body.applyTorque(angularSteering.angular, true);

        Debug.drawLine("agent vel", getPosition(), getPosition().cpy().add(getLinearVelocity()));
        Vector2 v = new Vector2();
        Debug.drawLine("agent orientation", getPosition(), angleToVector(v, getOrientation()).add(getPosition()));
    }

    protected LinePath<Vector2> convertToLinePath(GraphPath<MapNode> graphPath) {
        Array<Vector2> waypoints = new Array<>(graphPath.getCount());
        for (MapNode node : graphPath) {
            waypoints.add(new Vector2(node.x, node.y));
        }
        return new LinePath<>(waypoints, true);
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

    public GridPoint2 getGridPosition() {
        gridPos.set((int) getPosition().x, (int) getPosition().y);
        return gridPos;
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
