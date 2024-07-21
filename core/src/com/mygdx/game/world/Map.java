package com.mygdx.game.world;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entities.structures.Structure;

public class Map implements IndexedGraph<MapNode> {


    private final static String GROUND_LAYER = "ground layer";
    private final static String WALL_LAYER = "solid layer";
    private final Game game;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Array<Structure> structures;

    private final IndexedAStarPathFinder<MapNode> pathFinder;
    private final Array<MapNode> nodesList = new Array<>();
    private final MapNode[][] nodes;
    private final EuclideanHeuristic heuristic;

    public Map(Game game, String mapName) {
        this.game = game;
        // Load the map
        map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
        float unitScale = 1 / 64f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale, game.batch);
        renderer.setView(game.camera);
        structures = new Array<>();

        createBodies();

        TiledMapTileLayer groundLayer = getGroundLayer();
        heuristic = new EuclideanHeuristic();
        nodes = new MapNode[groundLayer.getWidth()][groundLayer.getHeight()];
        createGroundNodes();
        pathFinder = new IndexedAStarPathFinder<>(this, true);

    }
    private void createBodies() {
        TiledMapTileLayer wallLayer = getWallLayer();

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        for (int x = 0; x < wallLayer.getWidth(); x++) {
            for (int y = 0; y < wallLayer.getHeight(); y++) {
                if (wallLayer.getCell(x, y) != null) {
                    bodyDef.position.set(x + 0.5f, y + 0.5f);
                    Body wallBody = game.world.createBody(bodyDef);
                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.restitution = 0f;
                    wallBody.createFixture(fixtureDef);
                }
            }
        }
        shape.dispose();
    }

    private void createGroundNodes() {
        TiledMapTileLayer groundLayer = getGroundLayer();

        // create the nodes
        int index = 0;
        for (int y = 0; y < groundLayer.getWidth(); y++) {
            for (int x = 0; x < groundLayer.getHeight(); x++) {
                if (groundLayer.getCell(x, y) != null) {
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
            if (nodes[x-1][y] != null && nodes[x][y-1] != null)
                addNodeNeighbour(x - 1, y - 1, node, 1.4f);
            if (nodes[x+1][y] != null && nodes[x][y+1] != null)
                addNodeNeighbour(x + 1, y + 1, node, 1.4f);
            if (nodes[x-1][y] != null && nodes[x][y+1] != null)
                addNodeNeighbour(x - 1, y + 1, node, 1.4f);
            if (nodes[x+1][y] != null && nodes[x][y-1] != null)
                addNodeNeighbour(x + 1, y - 1, node, 1.4f);
        }
    }

    public TiledMapTileLayer getGroundLayer() {
        return (TiledMapTileLayer) map.getLayers().get(GROUND_LAYER);
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

    public void render() {
        renderer.render();
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

}