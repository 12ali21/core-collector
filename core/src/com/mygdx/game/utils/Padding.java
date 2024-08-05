package com.mygdx.game.utils;

public class Padding {
    public final float top, left, bottom, right;

    public Padding(float top, float left, float bottom, float right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Padding() {
        this.top = 0;
        this.left = 0;
        this.bottom = 0;
        this.right = 0;
    }
}
