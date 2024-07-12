package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.entities.Drawable;
import com.mygdx.game.entities.HoveredTile;
import com.mygdx.game.entities.Updatable;

import java.util.ArrayList;

public class World implements Drawable, Updatable {
    private static class Map {
        private final TiledMap map;
        private final OrthogonalTiledMapRenderer renderer;
        private final static String GROUND_LAYER = "ground layer";
        private final static String WALL_LAYER = "solid layer";

        public Map(Batch batch, OrthographicCamera camera, String mapName) {
            // Load the map
            map = new TmxMapLoader().load("maps/" + mapName + ".tmx");

            float unitScale = 1/64f;
            renderer = new OrthogonalTiledMapRenderer(map, unitScale, batch);
            renderer.setView(camera);
        }
        public TiledMapTileLayer getGroundLayer() {
            return (TiledMapTileLayer) map.getLayers().get(GROUND_LAYER);
        }

        public TiledMapTileLayer getWallLayer() {
            return (TiledMapTileLayer) map.getLayers().get(WALL_LAYER);
        }

        public void render() {
            renderer.render();
        }
    }

    private final Batch batch;
    private final OrthographicCamera camera;

    private final Map map;
    private final TiledMapTileLayer groundLayer;
    private final TiledMapTileLayer wallLayer;

    private final HoveredTile hoveredTile;

    private final ArrayList<Updatable> updatableList = new ArrayList<Updatable>();
    private final ArrayList<Drawable> drawableList = new ArrayList<Drawable>();


    public World(Batch batch, OrthographicCamera camera) {
        this.batch = batch;
        this.camera = camera;
        map = new Map(batch, camera, "testing");
        groundLayer = map.getGroundLayer();
        wallLayer = map.getWallLayer();

        hoveredTile = new HoveredTile(batch);
        drawableList.add(hoveredTile);
    }

    @Override
    public void update(float delta) {
        hoveredTile.findPosition(camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)));

        for (Updatable updatable : updatableList) {
            updatable.update(delta);
        }
    }

    @Override
    public void render() {
        map.render();
        batch.begin();
        for (Drawable drawable : drawableList) {
            drawable.render();
        }
        batch.end();
    }
}
