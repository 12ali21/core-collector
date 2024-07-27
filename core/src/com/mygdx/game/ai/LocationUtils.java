package com.mygdx.game.ai;

import com.badlogic.gdx.math.Vector2;

public class LocationUtils {

    public static float vectorToAngle(Vector2 v) {
        return v.angleRad();
    }

    public static Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = (float) Math.cos(angle);
        outVector.y = (float) Math.sin(angle);
        return outVector;
    }
}
