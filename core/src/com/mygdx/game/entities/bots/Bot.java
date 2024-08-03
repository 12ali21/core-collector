package com.mygdx.game.entities.bots;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Selectable;
import com.mygdx.game.ai.agents.BotAgent;
import com.mygdx.game.entities.others.EntityObj;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class Bot extends EntityObj implements Selectable {
    private final static float BOUNDS_OFFSET = 0.1f;
    private final static float SIZE = 0.5f;
    private final BotAgent agent;
    private Rectangle bounds;

    public Bot(Game game, Vector2 position) {
        super(game);
        setRenderPriority(10);
        sprite = new Sprite(TextureAssets.get(TextureAssets.BOT_TEXTURE));
        sprite.setSize(SIZE, SIZE);
        sprite.setOriginCenter();
        sprite.setPosition(position.x, position.y);

        agent = new BotAgent(game, position);
        updateBounds(position);
    }

    private void updateBounds(Vector2 position) {
        if (bounds == null) {
            bounds = new Rectangle();
        }
        bounds.set(position.x - SIZE / 2f - BOUNDS_OFFSET,
                position.y - SIZE / 2f - BOUNDS_OFFSET,
                SIZE + 2 * BOUNDS_OFFSET,
                SIZE + 2 * BOUNDS_OFFSET);
    }

    @Override
    public void render() {
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        agent.update();

        sprite.setOriginBasedPosition(agent.getPosition().x, agent.getPosition().y);
        sprite.setRotation(MathUtils.radiansToDegrees * agent.getOrientation() - 90);
        updateBounds(agent.getPosition());
    }

    @Override
    public Vector2 getCenter() {
        return agent.getPosition();
    }

    @Override
    public Rectangle getSelectableBounds() {
        return bounds;
    }

    @Override
    public void setTargetPosition(float x, float y) {
        agent.setMoveToTarget(x, y);
    }

    @Override
    public int getPriority() {
        return getRenderPriority();
    }
}