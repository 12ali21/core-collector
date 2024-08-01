package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.world.Game;

import java.util.Arrays;

public class SquareFormationPattern implements FormationPattern<Vector2> {
    private static final float DISTANCE = 1f;
    private final Game game;
    private final FormationAnchor anchor;
    private final int SLOT_SIZE = 5; // should be odd
    private final float[][] slotScores = new float[SLOT_SIZE][SLOT_SIZE];
    private final int[][] slotOccupation = new int[SLOT_SIZE][SLOT_SIZE]; //  -1 = free, -2 = occupied
    private final Vector2 lastAnchorPosition = new Vector2();
    private Vector2 tmp = new Vector2();
    private GridPoint2 tmpGrid = new GridPoint2();
    private int numberOfSlots;

    public SquareFormationPattern(Game game, FormationAnchor anchor) {
        this.game = game;
        this.anchor = anchor;
        lastAnchorPosition.set(anchor.getPosition());
        // un-occupy all slots
        for (int[] ints : slotOccupation) {
            Arrays.fill(ints, -1);
        }
    }


    @Override
    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    private Vector2 getSlotPosition(int x, int y) {
        return getRelativeSlotPosition(x, y).rotateRad(anchor.getOrientation()).add(anchor.getPosition());
    }

    private Vector2 getRelativeSlotPosition(int x, int y) {
        // (0, 0) for x, y is in top left corner so
        x -= SLOT_SIZE / 2;
        y = -y;

        tmp.x = x * DISTANCE;
        tmp.y = y * DISTANCE - 1f; // -1f since the anchor is 1f above the formation
        tmp.rotateRad(-MathUtils.HALF_PI);
        return tmp;
    }

    private boolean updateOccupations() {
//        if (lastAnchorPosition.epsilonEquals(anchor.getPosition(), 0.1f)) return;
        boolean changed = false;
        lastAnchorPosition.set(anchor.getPosition());

        // update occupations
        for (int i = 0; i < slotOccupation.length; i++) {
            for (int j = 0; j < slotOccupation[i].length; j++) {
                tmp = getSlotPosition(i, j);
                if (game.map.isTileWall((int) tmp.x, (int) tmp.y)) {
                    if (slotOccupation[i][j] != -2) {
                        slotOccupation[i][j] = -2;
                        changed = true;
                    }
                } else {
                    if (slotOccupation[i][j] == -2) {
                        slotOccupation[i][j] = -1;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Calculate the scores for each slot based on the distance to the anchor.
     * If a slot is occupied, the score is set to -1f.
     */
    private GridPoint2 findBestSlot(Vector2 position) {
        float maxScore = -1f;
        int bestX = 0;
        int bestY = 0;
        for (int i = 0; i < slotScores.length; i++) {
            for (int j = 0; j < slotScores[i].length; j++) {
                if (slotOccupation[i][j] == -1) { // if slot is free
                    slotScores[i][j] = 1f / (1f + getRelativeSlotPosition(i, j).len());
                    if (slotScores[i][j] > maxScore) {
                        maxScore = slotScores[i][j];
                        bestX = i;
                        bestY = j;
                    }
                } else {
                    slotScores[i][j] = -1f;
                }
            }
        }
        position.set(getRelativeSlotPosition(bestX, bestY));
        tmpGrid.set(bestX, bestY);
        return tmpGrid;
    }

    @Override
    public Location<Vector2> calculateSlotLocation(Location<Vector2> outLocation, int slotNumber) {
        if (updateOccupations()) {
            for (int i = 0; i < slotOccupation.length; i++) {
                for (int j = 0; j < slotOccupation[i].length; j++) {
                    if (slotOccupation[i][j] != -2) {
                        slotOccupation[i][j] = -1;
                    }
                }
            }
        } else {
            for (int i = 0; i < slotOccupation.length; i++) {
                for (int j = 0; j < slotOccupation[i].length; j++) {
                    if (slotOccupation[i][j] == slotNumber) {
                        tmp = getRelativeSlotPosition(i, j);
                        outLocation.getPosition().set(tmp);
                        return outLocation;
                    }
                }
            }
        }
        // if no slot is found, find the best one
        tmpGrid = findBestSlot(tmp);
        slotOccupation[tmpGrid.x][tmpGrid.y] = slotNumber;
        outLocation.getPosition().set(tmp);
        return outLocation;
    }

    @Override
    public boolean supportsSlots(int slotCount) {
        return slotCount <= SLOT_SIZE * SLOT_SIZE;
    }
}
