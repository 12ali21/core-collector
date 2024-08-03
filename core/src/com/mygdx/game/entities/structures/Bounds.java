package com.mygdx.game.entities.structures;

public class Bounds {
    // Bottom left corner
    public int x;
    public int y;
    public int width;
    public int height;

    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean inBounds(float x, float y) {
        return x > this.x && x < this.x + width && y > this.y && y < this.y + width;
    }
}
