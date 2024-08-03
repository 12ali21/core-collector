package com.mygdx.game.entities.structures;

import com.badlogic.gdx.math.Rectangle;

public class Bounds {
    // Bottom left corner
    public int x;
    public int y;
    public int width;
    public int height;

    private final Rectangle rect;

    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        rect = new Rectangle(x, y, width, height);
    }

    public Rectangle toRectangle() {
        return rect;
    }
}
