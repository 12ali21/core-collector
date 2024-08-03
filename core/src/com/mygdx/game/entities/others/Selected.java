package com.mygdx.game.entities.others;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Selectable;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class Selected extends Entity {
    private static final float SELECTED_OFFSET = 0.1f;
    private final NinePatch selectNinePatch;
    private final Rectangle bounds;
    private final Vector2 center;
    private Selectable entity;


    public Selected(Game game) {
        super(game);

        selectNinePatch = new NinePatch(
                TextureAssets.get(TextureAssets.SELECTED_TILE_TEXTURE),
                4, 4, 4, 4);

        selectNinePatch.scale(1f / Constants.TILE_SIZE, 1f / Constants.TILE_SIZE);
        Color color = new Color(1, 1, 1, 0.2f);
        selectNinePatch.setColor(color);
        bounds = new Rectangle();
        center = new Vector2();
    }

    @Override
    public void render() {
        if (entity != null) {
            selectNinePatch.draw(game.getBatch(),
                    bounds.x + SELECTED_OFFSET,
                    bounds.y + SELECTED_OFFSET,
                    bounds.width - 2 * SELECTED_OFFSET,
                    bounds.height - 2 * SELECTED_OFFSET
            );
        }
    }

    @Override
    public void update(float deltaTime) {
        if (entity != null) {
            bounds.set(entity.getSelectableBounds());
        }
    }

    @Override
    public Vector2 getCenter() {
        return bounds.getCenter(center);
    }

    public Selectable getEntity() {
        return entity;
    }

    public void setEntity(Selectable entity) {
        this.entity = entity;
        if (entity != null) {
            setRenderPriority(entity.getPriority() - 1);
        }
    }
}
