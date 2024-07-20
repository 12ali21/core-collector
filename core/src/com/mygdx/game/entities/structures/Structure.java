package com.mygdx.game.entities.structures;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.HealthPoints;
import com.mygdx.game.world.World;

public abstract class Structure extends Entity implements HealthPoints.Callback {
    protected final Array<StructurePart> parts;
    protected final Bounds bounds;
    protected final HealthPoints health;

    public Structure(Builder builder) {
        super(builder.world);
        this.bounds = builder.bounds;
        this.parts = builder.parts;
        health = new HealthPoints(builder.maxHp, this);
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
    public void onDeath() {
        for (StructurePart part : parts) {
            part.kill();
        }
        kill();
    }

    @Override
    public void render() {
        for (StructurePart p : parts) {
            p.render();
        }
    }

    public abstract static class Builder {
        protected final World world;
        protected final Array<StructurePart> parts;
        protected float maxHp;
        protected Bounds bounds;
        protected int width;
        protected int height;

        private final Color GHOST_COLOR = new Color(0.2f, 1, 0.2f, 0.5f);
        private final Color GHOST_COLOR_INVALID = new Color(1, 0.2f, 0.2f, 0.5f);
        private final Array<Sprite> ghostParts;

        public Builder(World world) {
            this.world = world;
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
                ghost.draw(world.getBatch());
            }
        }

        public Builder setBounds(int x, int y) {
            bounds = new Bounds(x - width / 2, y - height / 2, width, height);
            for (StructurePart part : parts) {
                part.sprite.setOriginBasedPosition(x, y);
            }
            return this;
        }

        public Bounds getBounds() {
            return bounds;
        }

        public abstract Structure build();
    }

    public static class StructurePart extends Entity {
        public Sprite sprite;

        public StructurePart(World world, Sprite sprite) {
            super(world);
            this.sprite = sprite;
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