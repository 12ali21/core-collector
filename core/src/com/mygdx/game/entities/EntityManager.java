package com.mygdx.game.entities;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Renderable;
import com.mygdx.game.Selectable;
import com.mygdx.game.Updatable;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.others.Selected;
import com.mygdx.game.entities.structures.Bounds;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.world.Game;

import java.util.Iterator;

public class EntityManager extends InputAdapter implements Updatable, Renderable {
    private final Game game;

    // Entity lists
    private final Array<Entity> entitiesRender = new Array<>();
    private final Array<Entity> entitiesUpdate = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private boolean isSorted = false;

    // Selected Entity
    private final Selected selectedEntity;

    // Drag selecting
    private final Vector2 dragStart;
    private final Rectangle rectangleSelectingBounds;
    private final Selected rectangleSelected;
    private final Selectable rectangleSelectable;
    private boolean dragging = false;

    public EntityManager(Game game) {
        this.game = game;
        selectedEntity = new Selected(game);
        addEntity(selectedEntity);
        rectangleSelectingBounds = new Rectangle();
        dragStart = new Vector2();

        rectangleSelected = new Selected(game);
        addEntity(rectangleSelected);

        rectangleSelectable = new Selectable() {
            @Override
            public Rectangle getSelectableBounds() {
                return rectangleSelectingBounds;
            }

            @Override
            public void setTargetPosition(float x, float y) {

            }

            @Override
            public int getPriority() {
                return Integer.MAX_VALUE;
            }
        };
    }


    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 worldPos = game.unproject(screenX, screenY);

        if (!dragging) {
            dragging = true;
            dragStart.set(worldPos.x, worldPos.y);
            rectangleSelectingBounds.x = worldPos.x;
            rectangleSelectingBounds.y = worldPos.y;
        } else {
            if (worldPos.x < dragStart.x) {
                rectangleSelectingBounds.width = dragStart.x - worldPos.x;
                rectangleSelectingBounds.x = worldPos.x;
            } else {
                rectangleSelectingBounds.width = worldPos.x - dragStart.x;
                rectangleSelectingBounds.x = dragStart.x;
            }

            if (worldPos.y < dragStart.y) {
                rectangleSelectingBounds.height = dragStart.y - worldPos.y;
                rectangleSelectingBounds.y = worldPos.y;
            } else {
                rectangleSelectingBounds.height = worldPos.y - dragStart.y;
                rectangleSelectingBounds.y = dragStart.y;
            }
        }


        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 selectedPosition = game.unproject(screenX, screenY);
        if (button == Input.Buttons.LEFT) {
            Selectable entity = selectableAt(selectedPosition.x, selectedPosition.y);
            selectedEntity.setEntity(entity);
            return entity != null;
        } else if (button == Input.Buttons.RIGHT) {
            if (selectedEntity.getEntity() != null) {
                selectedEntity.getEntity().setTargetPosition(selectedPosition.x, selectedPosition.y);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        rectangleSelectingBounds.set(0, 0, 0, 0);
        return false;
    }

    private Selectable selectableAt(float x, float y) {
        Selectable best = null;
        int bestPriority = Integer.MIN_VALUE;

        for (Entity entity : entitiesUpdate) {
            if (entity instanceof Selectable) {
                Selectable s = (Selectable) entity;
                if (s.getSelectableBounds().contains(x, y)) {
                    if (s.getPriority() > bestPriority) {
                        best = s;
                        bestPriority = s.getPriority();
                    }
                }
            }
        }
        return best;
    }

    @Override
    public void update(float deltaTime) {
        updateEntities(deltaTime);

        if (dragging) {
            rectangleSelected.setEntity(rectangleSelectable);
        } else {
            rectangleSelected.setEntity(null);
        }

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

    public void updateRenderingPriorities() {
        isSorted = false;
    }
}
