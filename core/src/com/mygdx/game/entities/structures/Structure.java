package com.mygdx.game.entities.structures;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mygdx.game.Selectable;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.others.HealthPoints;
import com.mygdx.game.utils.Padding;
import com.mygdx.game.world.Game;

public abstract class Structure extends Entity implements Selectable {
    protected final Array<StructurePart> parts;
    protected final Bounds bounds;
    protected final HealthPoints health;

    public Structure(Builder builder) {
        super(builder.game);
        this.bounds = builder.bounds;
        this.parts = builder.parts.values().toArray();
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

    @Override
    public Rectangle getSelectableBounds() {
        return bounds.toRectangle();
    }

    @Override
    public void setTargetPosition(float x, float y) {
        // nothing for now (maybe prioritize a target in turret? or face a certain direction?)
    }

    @Override
    public int getPriority() {
        return getRenderPriority();
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
        private final OrderedMap<String, StructurePart> parts;
        private final Color GHOST_COLOR = new Color(0.2f, 1, 0.2f, 0.5f);
        private final Color GHOST_COLOR_INVALID = new Color(1, 0.2f, 0.2f, 0.5f);
        private final OrderedMap<String, Sprite> ghostParts;
        protected float maxHp;
        protected Bounds bounds;
        protected int width;
        protected int height;
        /**
         * An offset to scale the ghost icon
         */
        protected Padding GHOST_SIZE_OFFSET;


        public Builder(Game game) {
            this.game = game;
            parts = new OrderedMap<>();
            ghostParts = new OrderedMap<>();
        }

        protected void addPart(String name, StructurePart part) {
            parts.put(name, part);
            Sprite ghost = new Sprite(part.sprite);
            ghost.setColor(GHOST_COLOR);
            ghostParts.put(name, ghost);
        }

        public void setGhostPosition(float x, float y) {
            for (Sprite ghost : ghostParts.values()) {
                ghost.setOriginBasedPosition(x, y);
            }
        }

        public void setGhostValid(boolean valid) {
            for (Sprite ghost : ghostParts.values()) {
                ghost.setColor(valid ? GHOST_COLOR : GHOST_COLOR_INVALID);
            }
        }

        public Texture getIconTexture() {
            Sprite ghost = ghostParts.values().next();
            int width = ghost.getRegionWidth();
            int height = ghost.getRegionHeight();
            if (GHOST_SIZE_OFFSET == null) {
                GHOST_SIZE_OFFSET = new Padding();
            }
            Pixmap pixmap = new Pixmap((int) (width + GHOST_SIZE_OFFSET.left + GHOST_SIZE_OFFSET.right),
                    (int) (height + GHOST_SIZE_OFFSET.top + GHOST_SIZE_OFFSET.bottom),
                    Pixmap.Format.RGBA8888
            );


            for (Sprite part : ghostParts.values()) {
                part.getTexture().getTextureData().prepare();
                Pixmap partPixmap = part.getTexture().getTextureData().consumePixmap();
                pixmap.drawPixmap(partPixmap,
                        (int) (((part.getWidth() / 2 - part.getOriginX()) / part.getWidth()) * width + GHOST_SIZE_OFFSET.left),
                        (int) (((part.getHeight() / 2 - part.getOriginY()) / part.getHeight()) * height + GHOST_SIZE_OFFSET.top)
                );
            }
            return new Texture(pixmap);
        }

        public void renderGhost() {
            for (Sprite ghost : ghostParts.values()) {
                ghost.draw(game.getBatch());
            }
        }

        public void setBounds(int x, int y) {
            bounds = new Bounds(x - width / 2, y - height / 2, width, height);
            for (StructurePart part : parts.values()) {
                part.sprite.setOriginBasedPosition(x, y);
            }
        }

        public Bounds getBounds() {
            return bounds;
        }

        public abstract Structure build();


        public OrderedMap<String, StructurePart> getParts() {
            return parts;
        }
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
