package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class HoveredTile implements Drawable {

    private Texture texture;
    private Sprite sprite;
    private Batch batch;
    private int x;
    private int y;
    public HoveredTile(Batch batch) {
        this.batch = batch;
        texture = new Texture(Gdx.files.internal("sprites/hovered_tile.png"));
        sprite = new Sprite(texture);
        sprite.setSize(1, 1);
    }

    public void findPosition(Vector3 unprojected) {
        x = (int) Math.floor(unprojected.x);
        y = (int) Math.floor(unprojected.y);
        sprite.setPosition(x, y);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }
}
