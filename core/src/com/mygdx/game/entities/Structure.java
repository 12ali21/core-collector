package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;

public abstract class Structure extends Entity {
    public abstract static class Builder {
        protected final World world;
        private final Color GHOST_COLOR = new Color(0.2f, 1, 0.2f, 0.5f);
        protected Bounds bounds;
        protected final Array<StructurePart> parts;
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

        public void renderGhost() {
            for (Sprite ghost : ghostParts) {
                ghost.draw(world.getBatch());
            }
        }

        public Builder setBounds(int x, int y, int width, int height) {
            bounds = new Bounds(x - width / 2, y - height / 2, width, height);
            for (StructurePart part: parts) {
                part.sprite.setOriginBasedPosition(x, y);
            }
            return this;
        }

        public abstract Structure build();
    }


    protected final Array<StructurePart> parts;
    protected final Bounds bounds;
    public Structure(Builder builder) {
        super(builder.world);
        this.bounds = builder.bounds;
        this.parts = builder.parts;
    }

    public Array<StructurePart> getParts() {
        return parts;
    }

    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render() {
        for (StructurePart p : parts) {
            p.render();
        }
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
