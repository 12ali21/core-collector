package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Constants;
import com.mygdx.game.world.Game;

public class HoveredTile extends Entity {

    private final Sprite sprite;
    private final Batch batch;

    int x;
    int y;

    public HoveredTile(Game game) {
        super(game);
        this.batch = game.getBatch();
        Texture texture = assets.get(Constants.HOVERED_TILE_TEXTURE, Texture.class);
        sprite = new Sprite(texture);
        sprite.setSize(1, 1);
    }

    public void findPosition(Vector3 unprojected) {
        x = (int) Math.floor(unprojected.x);
        y = (int) Math.floor(unprojected.y);
    }

    @Override
    public Vector2 getCenter() {
        return new Vector2(x + 0.5f, y + 0.5f);
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
