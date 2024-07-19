package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.StructureBuilder;
import com.mygdx.game.entities.turret.Turret;

import java.util.ArrayList;
import java.util.Iterator;

public class World implements Drawable, Updatable {

    private final AssetManager assets;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Map map;
    private final StructureBuilder structureBuilder = new StructureBuilder(this);
    private final HoveredTile hoveredTile;
    private final ArrayList<Entity> entitiesRender = new ArrayList<>();
    private final ArrayList<Entity> entitiesUpdate = new ArrayList<>();
    private final ArrayList<Entity> entitiesToAdd = new ArrayList<>();
    private boolean isSorted = false;

    public World(AssetManager assets, Batch batch, OrthographicCamera camera) {
        this.assets = assets;
        this.batch = batch;
        this.camera = camera;
        map = new Map(batch, camera, "testing");

        hoveredTile = new HoveredTile(this);
        addEntity(hoveredTile);
    }

    public Batch getBatch() {
        return batch;
    }

    public AssetManager getAssets() {
        return assets;
    }

    @Override
    public void update(float delta) {
        Vector3 mousePos = unproject(Gdx.input.getX(), Gdx.input.getY());
        hoveredTile.findPosition(mousePos);
        if (Gdx.input.justTouched()) {
            for (Entity entity : entitiesUpdate) {
                if (entity instanceof Turret) {
                    Turret turret = (Turret) entity;
                    turret.setTarget(mousePos.x, mousePos.y);
                }
            }
        }

        structureBuilder.update(delta);

        for (Iterator<Entity> itr = entitiesUpdate.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.update(delta);
            } else {
                itr.remove();
            }
        }

        registerEntities();
    }

    public Vector3 unproject(float x, float y) {
        return camera.unproject(new Vector3(x, y, 0));
    }

    @Override
    public void render() {
        map.render();
        batch.begin();
        structureBuilder.render();

        if(!isSorted) {
            entitiesRender.sort((a, b) -> Float.compare(a.getRenderPriority(), b.getRenderPriority()));
            isSorted = true;
        }

        for (Iterator<Entity> itr = entitiesRender.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.render();
            } else {
                itr.remove();
            }
        }
        batch.end();
    }

    /**
     * Add an entity to the world
     */
    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Add an entity to the world and add it as a structure to the map
     */
    public void addStructure(Structure structure) {
        Bounds bounds = structure.getBounds();
        map.putStructure(structure, bounds.x, bounds.y, bounds.width, bounds.height);

        entitiesToAdd.add(structure);
    }

    public boolean areTilesOccupied(int x, int y, int width, int height) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (map.isTileOccupied(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void registerEntities() {
        if (entitiesToAdd.isEmpty()) {
            return;
        }
        for (Entity e : entitiesToAdd) {
            if (e instanceof Structure) {
                Structure s = (Structure) e;
                for (Entity e2 : s.getParts()) {
                    entitiesRender.add(e2);
                }
            } else {
                entitiesRender.add(e);
            }

            entitiesUpdate.add(e);
            isSorted = false;
        }
        entitiesToAdd.clear();
    }

    public Array<CollidedEntity> checkCollisions(Vector2 v1, Vector2 v2, Entity entity) {
        Array<CollidedEntity> collisions = new Array<>();

        TiledMapTileLayer wallLayer = map.getWallLayer();

        int x1 = v2.x > v1.x ? (int) v1.x : (int) v2.x;
        int y1 = v2.y > v1.y ? (int) v1.y : (int) v2.y;
        int x2 = v2.x > v1.x ? (int) v2.x + 1: (int) v1.x + 1;
        int y2 = v2.y > v1.y ? (int) v2.y + 1: (int) v1.y + 1;


        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                if (wallLayer.getCell(i, j) != null) {
                    Rectangle r = new Rectangle(i, j, 1, 1);
//                    Debug.drawRect(r.toString(), r);
                    if (r.contains(v1) || r.contains(v2) || Intersector.intersectSegmentRectangle(v1, v2, r)) {
                        Vector2 center = new Vector2();
                        r.getCenter(center);
                        collisions.add(new CollidedEntity(center, EntityType.WALL));
                    }
                }
            }
        }
        return collisions;
    }

    private static class Map {
        private final static String GROUND_LAYER = "ground layer";
        private final static String WALL_LAYER = "solid layer";
        private final TiledMap map;
        private final OrthogonalTiledMapRenderer renderer;
        Structure[][] structures;

        public Map(Batch batch, OrthographicCamera camera, String mapName) {
            // Load the map
            map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
            structures = new Structure[map.getProperties().get("width", Integer.class)][map.getProperties().get("height", Integer.class)];
            float unitScale = 1 / 64f;
            renderer = new OrthogonalTiledMapRenderer(map, unitScale, batch);
            renderer.setView(camera);
        }

        public TiledMapTileLayer getGroundLayer() {
            return (TiledMapTileLayer) map.getLayers().get(GROUND_LAYER);
        }

        public TiledMapTileLayer getWallLayer() {
            return (TiledMapTileLayer) map.getLayers().get(WALL_LAYER);
        }

        public boolean isTileOccupied(int x, int y) {
            TiledMapTileLayer wallLayer = getWallLayer();
            if (x < 0 || x >= structures.length || y < 0 || y >= structures[0].length) {
                return true;
            }
            return wallLayer.getCell(x, y) != null || structures[x][y] != null;
        }

        public void putStructure(Structure structure, int x, int y, int width, int height) {
            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    structures[i][j] = structure;
                }
            }
        }

        public Structure getStructure(int x, int y) {
            return structures[x][y];
        }

        public void render() {
            renderer.render();
        }
    }
}
