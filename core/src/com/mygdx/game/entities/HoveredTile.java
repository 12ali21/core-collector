package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class HoveredTile extends Entity {

    private Texture texture;
    private Sprite sprite;
    private Batch batch;

    public HoveredTile(World world) {
        super(world);
        this.batch = world.getBatch();
        texture = assets.get("sprites/hovered_tile.png", Texture.class);
        sprite = new Sprite(texture);
        sprite.setSize(1, 1);
    }

    public void findPosition(Vector3 unprojected) {
        bounds.x = (int) Math.floor(unprojected.x);
        bounds.y = (int) Math.floor(unprojected.y);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        sprite.setPosition(bounds.x, bounds.y);
    }
}
