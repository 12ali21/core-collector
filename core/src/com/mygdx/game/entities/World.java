package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.StructureBuilder;
import com.mygdx.game.entities.*;
import com.mygdx.game.entities.turret.Turret;
import com.mygdx.game.entities.turret.TurretBase;

import java.util.ArrayList;
import java.util.Iterator;

public class World implements Drawable, Updatable {

    private static class Map {
        private final TiledMap map;
        private final OrthogonalTiledMapRenderer renderer;
        private final static String GROUND_LAYER = "ground layer";
        private final static String WALL_LAYER = "solid layer";

        Entity[][] entities;
        public Map(Batch batch, OrthographicCamera camera, String mapName) {
            // Load the map
            map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
            entities = new Entity[map.getProperties().get("width", Integer.class)][map.getProperties().get("height", Integer.class)];
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

        public void putEntity(Entity entity, int x, int y) {
            entities[x][y] = entity;
        }

        public Entity getEntity(int x, int y) {
            return entities[x][y];
        }

        public void render() {
            renderer.render();
        }
    }

    private final AssetManager assets;
    private final Batch batch;
    private final OrthographicCamera camera;

    private final Map map;
    private final TiledMapTileLayer groundLayer;
    private final TiledMapTileLayer wallLayer;

    private final StructureBuilder structureBuilder = new StructureBuilder(this);

    private final HoveredTile hoveredTile;

    private final ArrayList<Entity> entities = new ArrayList<>();
    private final ArrayList<Entity> entitiesToAdd = new ArrayList<>();

    private Turret turret;


    public World(AssetManager assets, Batch batch, OrthographicCamera camera) {
        this.assets = assets;
        this.batch = batch;
        this.camera = camera;
        map = new Map(batch, camera, "testing");
        groundLayer = map.getGroundLayer();
        wallLayer = map.getWallLayer();

        hoveredTile = new HoveredTile(this);
        hoveredTile.addToWorld(false);

        turret = new Turret(this, new Vector2(2, 2), new TurretBase(this, new Vector2(2, 2)));
        turret.addToWorld(true);
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
            turret.setTarget(mousePos.x, mousePos.y);
//            entities.add(new Bullet(assets, batch, turret.getPosition(), 1f, 50));
        }

        structureBuilder.update(delta);

        for (Iterator<Entity> itr = entities.iterator(); itr.hasNext(); ) {
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

        for (Iterator<Entity> itr = entities.iterator(); itr.hasNext(); ) {
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
     * Add an entity to the world without adding it as structure to the map
     */
    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Add an entity to the world and add it as a structure to the map
     */
    public void addStructure(Entity entity, Bounds bounds) {
        entitiesToAdd.add(entity);
        for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                map.putEntity(entity, x, y);
            }
        }
    }

    private void registerEntities() {
        entities.addAll(entitiesToAdd);
        entitiesToAdd.clear();
    }
}
