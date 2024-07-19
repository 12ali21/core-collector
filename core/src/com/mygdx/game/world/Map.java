package com.mygdx.game.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.game.entities.structures.Structure;

public class Map {
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