package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ai.agents.EnemyAgent;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class RedCreep extends Enemy {

    private static final float ATTACKING_RANGE = 1;
    private static final float DAMAGE = 15;
    private static final float DAMAGE_COOLDOWN = 1f;

    public RedCreep(Game game, Vector2 position) {
        super(game, 30);
        Texture t = TextureAssets.get(TextureAssets.ENEMY_SMALL_TEXTURE);
        sprite = new Sprite(t);
        sprite.setSize(1, 1);
        sprite.setScale(0.8f);
        sprite.setOriginCenter();
        sprite.setOriginBasedPosition(position.x + 0.5f, position.y + 0.5f);
        health.setOffset(new Vector2(0, -0.5f));

        this.agent = new EnemyAgent(game, this, position, ATTACKING_RANGE, DAMAGE, DAMAGE_COOLDOWN);

        movementSpeed = 2;
    }

    public void stagger(float time) {
        agent.stagger(time);
    }

    @Override
    public void render() {
        super.render();
        sprite.draw(batch);
    }

    @Override
    public void update(float deltaTime) {
        agent.update(deltaTime);
        super.update(deltaTime);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
