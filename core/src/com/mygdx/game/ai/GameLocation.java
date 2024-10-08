package com.mygdx.game.ai;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

public class GameLocation implements Location<Vector2> {
    Vector2 position;
    float orientation;

    public GameLocation() {
        this(new Vector2());
    }

    public GameLocation(Vector2 position) {
        this.position = position;
        this.orientation = 0;
    }

    public GameLocation(Vector2 position, float orientation) {
        this.position = position;
        this.orientation = orientation;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
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
