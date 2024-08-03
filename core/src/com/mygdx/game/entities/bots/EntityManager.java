package com.mygdx.game.entities.bots;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Renderable;
import com.mygdx.game.Selectable;
import com.mygdx.game.Updatable;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.structures.Bounds;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

import java.util.Iterator;

public class EntityManager extends InputAdapter implements Updatable, Renderable {
    private final Game game;

    private final Array<Entity> entitiesRender = new Array<>();
    private final Array<Entity> entitiesUpdate = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private final NinePatch selectNinePatch;
    private boolean isSorted = false;
    private Selectable selected;
    private static final float SELECTED_OFFSET = 0.1f;

    public EntityManager(Game game) {
        this.game = game;
        selectNinePatch = new NinePatch(
                TextureAssets.get(TextureAssets.SELECTED_TILE_TEXTURE),
                4, 4, 4, 4);

        selectNinePatch.scale(1f / Constants.TILE_SIZE, 1f / Constants.TILE_SIZE);
        Color color = new Color(1, 1, 1, 0.2f);
        selectNinePatch.setColor(color);
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 selectedPosition = game.unproject(screenX, screenY);
        if (button == Input.Buttons.LEFT) {
            selected = selectableAt(selectedPosition.x, selectedPosition.y);
        } else if (button == Input.Buttons.RIGHT) {
            if (selected != null) {
                selected.setTargetPosition(selectedPosition.x, selectedPosition.y);
            }
        }
        return false;
    }

    private Selectable selectableAt(float x, float y) {
        for (Entity entity : entitiesUpdate) {
            if (entity instanceof Selectable) {
                Selectable s = (Selectable) entity;
                if (s.getSelectableBounds().contains(x, y)) {
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    public void update(float deltaTime) {
        updateEntities(deltaTime);

        registerEntities();
    }

    private void updateEntities(float deltaTime) {
        // Update entities
        for (Iterator<Entity> itr = entitiesUpdate.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.update(deltaTime);
            } else {
                if (entity instanceof Structure) { // since structure need to be removed from map
                    Structure s = (Structure) entity;
                    Bounds bounds = s.getBounds();
                    game.map.removeStructure(s, bounds.x, bounds.y, bounds.width, bounds.height);
                }
                entity.dispose();
                itr.remove();
            }
        }
    }

    @Override
    public void render() {
        // Sort entities by rendering order
        if (!isSorted) {
            entitiesRender.sort((a, b) -> Float.compare(a.getRenderPriority(), b.getRenderPriority()));
            isSorted = true;
        }

        drawSelected();

        // Render entities
        for (Iterator<Entity> itr = entitiesRender.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.render();
            } else {
                itr.remove();
            }
        }

    }

    private void drawSelected() {
        if (selected != null) {
            selectNinePatch.draw(game.getBatch(),
                    selected.getSelectableBounds().x + SELECTED_OFFSET,
                    selected.getSelectableBounds().y + SELECTED_OFFSET,
                    selected.getSelectableBounds().width - 2 * SELECTED_OFFSET,
                    selected.getSelectableBounds().height - 2 * SELECTED_OFFSET
            );
        }
    }

    /**
     * Add an entity to the world
     */
    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Add a structure to the world and map
     */
    public void addStructure(Structure structure) {
        Bounds bounds = structure.getBounds();
        game.map.putNewStructure(structure, bounds.x, bounds.y, bounds.width, bounds.height);

        entitiesToAdd.add(structure);
    }

    /**
     * Register new entities this frame to be updated and rendered
     */
    private void registerEntities() {
        if (entitiesToAdd.isEmpty()) {
            return;
        }
        for (Entity e : entitiesToAdd) {
            // If the entity is a structure, add its parts instead
            if (e instanceof Structure) {
                Structure s = (Structure) e;
                for (Entity e2 : s.getParts()) {
                    entitiesRender.add(e2);
                }
            }
            entitiesRender.add(e);
            entitiesUpdate.add(e);
        }
        isSorted = false;
        entitiesToAdd.clear();
    }
}
