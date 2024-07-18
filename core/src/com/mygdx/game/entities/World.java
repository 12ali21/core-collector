package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.StructureBuilder;
import com.mygdx.game.entities.turret.Turret;
import com.mygdx.game.entities.turret.Turrets;

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
    private final Turret testTurret;

    public World(AssetManager assets, Batch batch, OrthographicCamera camera) {
        this.assets = assets;
        this.batch = batch;
        this.camera = camera;
        map = new Map(batch, camera, "testing");

        hoveredTile = new HoveredTile(this);
        addEntity(hoveredTile);

        Turret.Builder builder = Turrets.basicTurret(this, 2, 2);
        testTurret = builder.build();
        addStructure(testTurret);
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
        if (Gdx.input.isTouched()) {
            testTurret.setTarget(mousePos.x, mousePos.y);
//            entities.add(new Bullet(assets, batch, turret.getPosition(), 1f, 50));
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
