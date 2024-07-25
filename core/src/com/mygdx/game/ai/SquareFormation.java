package com.mygdx.game.ai;

import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

public class SquareFormation implements FormationPattern<Vector2> {
    @Override
    public void setNumberOfSlots(int numberOfSlots) {

    }

    @Override
    public Location<Vector2> calculateSlotLocation(Location<Vector2> outLocation, int slotNumber) {
        return null;
    }

    @Override
    public boolean supportsSlots(int slotCount) {
        return false;
    }
}
