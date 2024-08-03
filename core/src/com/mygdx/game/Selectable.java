package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

public interface Selectable {
    Rectangle getSelectableBounds();

    /**
     * Gets called when the user right-click a position while this selectable is selected.
     *
     * @param x the x-position in world the mouse was right-clicked
     * @param y the y-position in world the mouse was right-clicked
     */
    void setTargetPosition(float x, float y);
}
