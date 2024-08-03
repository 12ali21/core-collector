package com.mygdx.game.entities.others;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Renderable;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class HealthPoints implements Renderable {
    private static final float YELLOW_THRESHOLD = 0.5f;
    private static final float RED_THRESHOLD = 0.2f;
    private final Vector2 offset = new Vector2(0, 0);
    private final float maxHp;
    private final Batch batch;
    private final Callback callback;
    private final Sprite border;
    private final Sprite fill;
    private final Texture greenBarTexture;
    private final Texture yellowBarTexture;
    private final Texture redBarTexture;
    private float height = 0.1f;
    private float width = 1;
    private float hp;

    public HealthPoints(Game game, float maxHp, Callback callback) {
        this.maxHp = maxHp;
        this.callback = callback;
        this.batch = game.getBatch();

        Texture t = TextureAssets.get(TextureAssets.HEALTH_BORDER_TEXTURE);
        border = new Sprite(t);
        border.setSize(width, height);
        border.setOriginCenter();

        greenBarTexture = TextureAssets.get(TextureAssets.GREEN_BAR_TEXTURE);
        yellowBarTexture = TextureAssets.get(TextureAssets.YELLOW_BAR_TEXTURE);
        redBarTexture = TextureAssets.get(TextureAssets.RED_BAR_TEXTURE);

        fill = new Sprite(greenBarTexture);
        fill.setSize(width, height);
        fill.setOriginCenter();

        hp = maxHp;
    }

    public void setHeight(float height) {
        this.height = height;
        border.setSize(width, height);
        border.setOriginCenter();
        fill.setSize(width, height);
    }

    public void setWidth(float width) {
        this.width = width;
        border.setSize(width, height);
        border.setOriginCenter();
        fill.setSize(width, height);
    }

    public void setOffset(Vector2 offset) {
        this.offset.set(offset);
    }

    private void updateHp() {
        if (hp < 0) {
            callback.onDeath();
        } else if (hp < maxHp) {
            if (hp < maxHp * RED_THRESHOLD) {
                fill.setTexture(redBarTexture);
            } else if (hp < maxHp * YELLOW_THRESHOLD) {
                fill.setTexture(yellowBarTexture);
            } else {
                fill.setTexture(greenBarTexture);
            }
            fill.setSize(width * hp / maxHp, height);
        } else {
            hp = maxHp;
            fill.setTexture(greenBarTexture);
        }
    }

    public void damage(float amount) {
        hp -= amount;
        updateHp();
    }

    public void heal(float amount) {
        hp += amount;
        updateHp();
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getHp() {
        return hp;
    }

    public void setPosition(Vector2 position) {
        Vector2 pos = position.cpy().add(offset);
        border.setOriginBasedPosition(pos.x, pos.y);
        fill.setPosition(pos.x - width / 2, pos.y - height / 2);
    }

    @Override
    public void render() {
        fill.draw(batch);
        border.draw(batch);
    }

    public interface Callback {
        void onDeath();
    }
}
