package com.mygdx.game.world.map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.generator.MapGenerator;

public class MapManager implements IndexedGraph<MapNode>, Disposable {

    public final static String WALL_LAYER = "solid layer";
    private final Game game;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Array<Structure> structures = new Array<>();

    private final IndexedAStarPathFinder<MapNode> pathFinder;
    private final Array<MapNode> nodesList = new Array<>();
    private final MapNode[][] nodes;
    private final EuclideanHeuristic heuristic;

    private final float width;
    private final float height;
    private final float TILE_SIZE = 64f;


    Texture groundTexture;

    public MapManager(Game game, String mapName) {
        this.game = game;
        // Load the map
        MapGenerator generator = new MapGenerator(12345);
//        map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
        map = generator.generate(200, 200);
        float unitScale = 1 / TILE_SIZE;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale, game.getBatch());
        renderer.setView(game.getCamera());

        groundTexture = TextureAssets.get(TextureAssets.GROUND_TEXTURE);
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TiledMapTileLayer wallLayer = getWallLayer();
        width = wallLayer.getWidth();
        height = wallLayer.getHeight();

        createBodies(wallLayer);
        heuristic = new EuclideanHeuristic();
        nodes = new MapNode[wallLayer.getWidth()][wallLayer.getHeight()];
        createGroundNodes(wallLayer);
        pathFinder = new IndexedAStarPathFinder<>(this, true);

//
    }

    private void createBodies(TiledMapTileLayer wallLayer) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        for (int x = 0; x < wallLayer.getWidth(); x++) {
            for (int y = 0; y < wallLayer.getHeight(); y++) {
                if (wallLayer.getCell(x, y) != null) {
                    bodyDef.position.set(x + 0.5f, y + 0.5f);
                    Body wallBody = game.getWorld().createBody(bodyDef);
                    wallBody.setUserData(CellBodyType.WALL);
                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.restitution = 0f;
                    wallBody.createFixture(fixtureDef).setUserData(CellBodyType.WALL);
                }
            }
        }
        shape.dispose();
    }

    private void createGroundNodes(TiledMapTileLayer wallLayer) {
        // create the nodes
        int index = 0;
        for (int y = 0; y < wallLayer.getWidth(); y++) {
            for (int x = 0; x < wallLayer.getHeight(); x++) {
                if (wallLayer.getCell(x, y) == null) {
                    MapNode node = new MapNode(index++, x, y);
                    nodesList.add(node);
                    nodes[x][y] = node;
                }
            }
        }

        // create the connections between nodes
        for (MapNode node : nodesList) {
            int x = node.x;
            int y = node.y;

            // Horizontal and vertical connections
            addNodeNeighbour(x - 1, y, node, 1);
            addNodeNeighbour(x + 1, y, node, 1);
            addNodeNeighbour(x, y - 1, node, 1);
            addNodeNeighbour(x, y + 1, node, 1);

            // Diagonal connections if possible
            if (nodes[x - 1][y] != null && nodes[x][y - 1] != null)
                addNodeNeighbour(x - 1, y - 1, node, 1.4f);
            if (nodes[x + 1][y] != null && nodes[x][y + 1] != null)
                addNodeNeighbour(x + 1, y + 1, node, 1.4f);
            if (nodes[x - 1][y] != null && nodes[x][y + 1] != null)
                addNodeNeighbour(x - 1, y + 1, node, 1.4f);
            if (nodes[x + 1][y] != null && nodes[x][y - 1] != null)
                addNodeNeighbour(x + 1, y - 1, node, 1.4f);
        }
    }

    public TiledMapTileLayer getWallLayer() {
        return (TiledMapTileLayer) map.getLayers().get(WALL_LAYER);
    }

    public boolean isTileOccupied(int x, int y) {
        if (x < 0 || x >= nodes.length || y < 0 || y >= nodes[0].length) {
            return true;
        }
        return nodes[x][y] == null || nodes[x][y].hasStructure();
    }

    public void putNewStructure(Structure structure, int x, int y, int width, int height) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                nodes[i][j].putStructure(structure);
            }
        }
        structures.add(structure);
    }

    public void removeStructure(Structure structure, int x, int y, int width, int height) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                nodes[i][j].putStructure(null);
            }
        }
        structures.removeValue(structure, true);
    }

    public Array<Structure> getStructures() {
        return structures;
    }

    //TODO: don't draw the whole ground, only the visible part
    private void drawGround(Batch batch) {
        batch.setColor(185f / 255f, 133f / 255f, 93f / 255f, 1f);
        int srcWidth = (int) (width * TILE_SIZE);
        int srcHeight = (int) (height * TILE_SIZE);

        batch.draw(groundTexture, 0, 0, width, height, 0, 0, srcWidth, srcHeight, false, false);
        batch.setColor(Color.WHITE);

    }

    public void render() {
        Batch batch = game.getBatch();
        renderer.setView(game.getCamera());
        batch.begin();
        drawGround(batch);
        renderer.renderTileLayer(getWallLayer());
        batch.end();
    }

    private void addNodeNeighbour(int x, int y, MapNode node, float cost) {
        if (x < 0 || x >= nodes.length || y < 0 || y >= nodes[0].length) {
            return;
        }
        MapNode neighbour = nodes[x][y];
        if (neighbour != null) {
            node.addConnection(neighbour, cost);
        }
    }

    public DefaultGraphPath<MapNode> findPath(int startX, int startY, int endX, int endY) {
        DefaultGraphPath<MapNode> path = new DefaultGraphPath<>();

        MapNode startNode = nodes[startX][startY];
        MapNode endNode = nodes[endX][endY];
        pathFinder.searchNodePath(startNode, endNode, heuristic, path);
        return path;
    }

    @Override
    public int getIndex(MapNode node) {
        return node.getIndex();
    }

    @Override
    public int getNodeCount() {
        return nodesList.size;
    }

    @Override
    public Array<Connection<MapNode>> getConnections(MapNode fromNode) {
        return fromNode.getConnections();
    }

    @Override
    public void dispose() {
        groundTexture.dispose();
    }

    public enum CellBodyType {
        WALL, STRUCTURE
    }

}