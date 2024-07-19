package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.world.World;

public class HoveredTile extends Entity {

    private final Sprite sprite;
    private final Batch batch;

    int x;
    int y;

    public HoveredTile(World world) {
        super(world);
        this.batch = world.getBatch();
        Texture texture = assets.get("sprites/hovered_tile.png", Texture.class);
        sprite = new Sprite(texture);
        sprite.setSize(1, 1);
    }

    public void findPosition(Vector3 unprojected) {
        x = (int) Math.floor(unprojected.x);
        y = (int) Math.floor(unprojected.y);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        sprite.setPosition(x, y);
    }
}
