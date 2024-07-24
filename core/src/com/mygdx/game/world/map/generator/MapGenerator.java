package com.mygdx.game.world.map.generator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.map.MapManager;

import java.util.Arrays;

public class MapGenerator {
    public static final boolean[][] TILE_DIRECTIONS = new boolean[47][];
    private static final double SCALE = 1 / 16f;
    private static final int TILE_SIZE = 64;
    private static final float WALL_THRESHOLD = 0.65f;

    static {
        // N, NE, E, SE, S, SW, W, NW
        TILE_DIRECTIONS[0] = new boolean[]{false, false, true, false, true, false, false, false};
        TILE_DIRECTIONS[1] = new boolean[]{false, false, false, false, true, false, true, false};
        TILE_DIRECTIONS[2] = new boolean[]{true, false, false, false, true, false, false, false};
        TILE_DIRECTIONS[3] = new boolean[]{false, false, true, false, true, false, true, false};
        TILE_DIRECTIONS[4] = new boolean[]{true, false, true, false, false, false, true, false};
        TILE_DIRECTIONS[5] = new boolean[]{false, false, false, false, false, false, true, false};
        TILE_DIRECTIONS[6] = new boolean[]{true, false, false, false, false, false, false, false};
        TILE_DIRECTIONS[7] = new boolean[]{true, false, true, false, true, false, true, false};
        TILE_DIRECTIONS[8] = new boolean[]{true, false, true, false, false, false, false, false};
        TILE_DIRECTIONS[9] = new boolean[]{true, false, false, false, false, false, true, false};
        TILE_DIRECTIONS[10] = new boolean[]{false, false, true, false, false, false, true, false};
        TILE_DIRECTIONS[11] = new boolean[]{true, false, true, false, true, false, false, false};
        TILE_DIRECTIONS[12] = new boolean[]{true, false, false, false, true, false, true, false};
        TILE_DIRECTIONS[13] = new boolean[]{false, false, false, false, true, false, false, false};
        TILE_DIRECTIONS[14] = new boolean[]{false, false, true, false, false, false, false, false};
        TILE_DIRECTIONS[15] = new boolean[]{false, false, true, true, true, false, false, false};
        TILE_DIRECTIONS[16] = new boolean[]{true, true, true, false, true, true, true, true};
        TILE_DIRECTIONS[17] = new boolean[]{true, true, true, true, true, false, true, true};
        TILE_DIRECTIONS[18] = new boolean[]{true, true, true, false, true, false, true, true};
        TILE_DIRECTIONS[19] = new boolean[]{true, false, true, true, true, true, true, false};
        TILE_DIRECTIONS[20] = new boolean[]{true, true, true, true, true, false, false, false};
        TILE_DIRECTIONS[21] = new boolean[]{true, false, false, false, true, true, true, true};
        TILE_DIRECTIONS[22] = new boolean[]{true, true, true, true, true, true, true, true};
        TILE_DIRECTIONS[23] = new boolean[]{true, true, true, false, false, false, false, false};
        TILE_DIRECTIONS[24] = new boolean[]{true, false, true, true, true, true, true, true};
        TILE_DIRECTIONS[25] = new boolean[]{true, true, true, true, true, true, true, false};
        TILE_DIRECTIONS[26] = new boolean[]{true, false, true, false, true, true, true, true};
        TILE_DIRECTIONS[27] = new boolean[]{true, true, true, true, true, false, true, false};
        TILE_DIRECTIONS[28] = new boolean[]{false, false, true, true, true, true, true, false};
        TILE_DIRECTIONS[30] = new boolean[]{false, false, false, false, false, false, false, false};
        TILE_DIRECTIONS[29] = new boolean[]{true, true, true, false, false, false, true, true};
        TILE_DIRECTIONS[31] = new boolean[]{false, false, false, false, true, true, true, false};
        TILE_DIRECTIONS[32] = new boolean[]{true, false, true, true, true, false, true, false};
        TILE_DIRECTIONS[33] = new boolean[]{true, false, true, false, true, true, true, false};
        TILE_DIRECTIONS[34] = new boolean[]{true, false, true, true, true, false, false, false};
        TILE_DIRECTIONS[35] = new boolean[]{true, false, false, false, true, true, true, false};
        TILE_DIRECTIONS[36] = new boolean[]{false, false, true, true, true, false, true, false};
        TILE_DIRECTIONS[37] = new boolean[]{false, false, true, false, true, true, true, false};
        TILE_DIRECTIONS[38] = new boolean[]{true, true, true, false, true, true, true, false};
        TILE_DIRECTIONS[39] = new boolean[]{true, false, false, false, false, false, true, true};
        TILE_DIRECTIONS[40] = new boolean[]{true, true, true, false, true, false, true, false};
        TILE_DIRECTIONS[41] = new boolean[]{true, false, true, false, true, false, true, true};
        TILE_DIRECTIONS[42] = new boolean[]{true, true, true, false, true, false, false, false};
        TILE_DIRECTIONS[43] = new boolean[]{true, false, false, false, true, false, true, true};
        TILE_DIRECTIONS[44] = new boolean[]{true, true, true, false, false, false, true, false};
        TILE_DIRECTIONS[45] = new boolean[]{true, false, true, false, false, false, true, true};
        TILE_DIRECTIONS[46] = new boolean[]{true, false, true, true, true, false, true, false};
    }

    public final long seed;
    TiledMapTileLayer wallLayer;
    private TiledMap map;
    private int width;
    private int height;

    public MapGenerator(long seed) {
        this.seed = seed;
    }

    public TiledMap generate(int width, int height) {
        if (map != null)
            throw new IllegalStateException("Map already generated");
        map = new TiledMap();
        this.width = width;
        this.height = height;

        Texture mountainWallTexture = TextureAssets.get(TextureAssets.DIRT_WALL_TEXTURE);
        TextureRegion[][] mountain = TextureRegion.split(mountainWallTexture, TILE_SIZE, TILE_SIZE);
        MapLayers layers = map.getLayers();
        wallLayer = new TiledMapTileLayer(width, height, TILE_SIZE, TILE_SIZE);
        wallLayer.setName(MapManager.WALL_LAYER);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float noise = OpenSimplex2S.noise2(seed, x * SCALE, y * SCALE);
                float normalNoise = (noise + 1) / 2f;
                // fill with mountain wall
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1 || (normalNoise > WALL_THRESHOLD)) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    wallLayer.setCell(x, y, cell);
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = wallLayer.getCell(x, y);
                if (cell != null)
                    cell.setTile(getTile(mountain, x, y));
            }
        }

        layers.add(wallLayer);
        return map;
    }

    private boolean isWall(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return true;
        }
        return wallLayer.getCell(x, y) != null;
    }

    //FIXME: four situations not handled. screenshotted
    private StaticTiledMapTile getTile(TextureRegion[][] tiles, int x, int y) {
        // default index where there is no wall around
        int index = 30;
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("Invalid coordinates: " + x + ", " + y);
        }
        boolean[] directions = new boolean[8];
        directions[0] = isWall(x, y + 1);
        directions[2] = isWall(x + 1, y);
        directions[4] = isWall(x, y - 1);
        directions[6] = isWall(x - 1, y);

        // Diagonal matters if both sides of the diagonal are walls
        directions[1] = directions[0] && directions[2] && isWall(x + 1, y + 1);
        directions[3] = directions[2] && directions[4] && isWall(x + 1, y - 1);
        directions[5] = directions[4] && directions[6] && isWall(x - 1, y - 1);
        directions[7] = directions[0] && directions[6] && isWall(x - 1, y + 1);

        for (int i = 0; i < TILE_DIRECTIONS.length; i++) {
            if (Arrays.equals(TILE_DIRECTIONS[i], directions)) {
                index = i;
                break;
            }
        }
        return breakIndex(index, tiles);
    }

    private StaticTiledMapTile breakIndex(int index, TextureRegion[][] tiles) {
        int i = index / tiles[0].length;
        int j = index % tiles[0].length;
        return new StaticTiledMapTile(tiles[i][j]);
    }

}
