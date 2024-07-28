package com.mygdx.game.entities.structures;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.others.HealthPoints;
import com.mygdx.game.world.Game;

public abstract class Structure extends Entity {
    protected final Array<StructurePart> parts;
    protected final Bounds bounds;
    protected final HealthPoints health;

    public Structure(Builder builder) {
        super(builder.game);
        this.bounds = builder.bounds;
        this.parts = builder.parts;
        health = new HealthPoints(game, builder.maxHp, () -> {
            for (StructurePart part : parts) {
                part.kill();
            }
            kill();
        });
    }

    public Array<StructurePart> getParts() {
        return parts;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public Vector2 getCenter() {
        return new Vector2(bounds.x + (float) bounds.width / 2, bounds.y + (float) bounds.height / 2);
    }

    public HealthPoints getHealth() {
        return health;
    }

    @Override
    public void render() {
        health.render();
    }

    @Override
    public void update(float deltaTime) {
        health.setPosition(getCenter());
    }

    public abstract static class Builder {
        protected final Game game;
        protected final Array<StructurePart> parts;
        private final Color GHOST_COLOR = new Color(0.2f, 1, 0.2f, 0.5f);
        private final Color GHOST_COLOR_INVALID = new Color(1, 0.2f, 0.2f, 0.5f);
        private final Array<Sprite> ghostParts;
        protected float maxHp;
        protected Bounds bounds;
        protected int width;
        protected int height;

        public Builder(Game game) {
            this.game = game;
            parts = new Array<>();
            ghostParts = new Array<>();
        }

        protected void addPart(StructurePart part) {
            parts.add(part);
            Sprite ghost = new Sprite(part.sprite);
            ghost.setColor(GHOST_COLOR);
            ghostParts.add(ghost);
        }

        public void setGhostPosition(float x, float y) {
            for (Sprite ghost : ghostParts) {
                ghost.setOriginBasedPosition(x, y);
            }
        }

        public void setGhostValid(boolean valid) {
            for (Sprite ghost : ghostParts) {
                ghost.setColor(valid ? GHOST_COLOR : GHOST_COLOR_INVALID);
            }
        }

        public void renderGhost() {
            for (Sprite ghost : ghostParts) {
                ghost.draw(game.getBatch());
            }
        }

        public void setBounds(int x, int y) {
            bounds = new Bounds(x - width / 2, y - height / 2, width, height);
            for (StructurePart part : parts) {
                part.sprite.setOriginBasedPosition(x, y);
            }
        }

        public Bounds getBounds() {
            return bounds;
        }

        public abstract Structure build();
    }

    public static class StructurePart extends Entity {
        public Sprite sprite;

        public StructurePart(Game game, Sprite sprite) {
            super(game);
            this.sprite = sprite;
        }

        @Override
        public Vector2 getCenter() {
            return new Vector2(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY());
        }

        @Override
        public void render() {
            sprite.draw(batch);
        }

        @Override
        public void update(float deltaTime) {

        }
    }
}
