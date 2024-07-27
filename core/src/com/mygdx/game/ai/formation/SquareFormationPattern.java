package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

public class SquareFormationPattern implements FormationPattern<Vector2> {
    private int numberOfSlots;

    @Override
    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    @Override
    public Location<Vector2> calculateSlotLocation(Location<Vector2> outLocation, int slotNumber) {
        float side = (float) Math.ceil(Math.sqrt(numberOfSlots));
        float offsetX = -side;
        float offsetY = -(side - 1f) / 2f;
        float x = slotNumber % side + offsetX;
        float y = (float) Math.floor(slotNumber / side) + offsetY;
        outLocation.getPosition().set(x, y);
        return outLocation;
    }

    @Override
    public boolean supportsSlots(int slotCount) {
        return true; //TODO: Handle this
    }
}
