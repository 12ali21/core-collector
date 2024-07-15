package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.World;

public class HoveredTile extends Entity {

    private Texture texture;
    private Sprite sprite;
    private Batch batch;
    private int x;
    private int y;
    public HoveredTile(World world) {
        super(world, new Vector2(0, 0));
        this.batch = world.getBatch();
        texture = assets.get("sprites/hovered_tile.png", Texture.class);
        sprite = new Sprite(texture);
        sprite.setSize(1, 1);
    }

    public void findPosition(Vector3 unprojected) {
        x = (int) Math.floor(unprojected.x);
        y = (int) Math.floor(unprojected.y);
        position.set(x, y);
        sprite.setPosition(x, y);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {

    }
}
