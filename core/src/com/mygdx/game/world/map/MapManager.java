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
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.generator.MapGenerator;

public class MapManager implements IndexedGraph<MapNode>, Disposable {

    public final static String WALL_LAYER = "solid layer";
    private final Game game;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final Array<Structure> structures = new Array<>();
    private final EuclideanHeuristic heuristic;
    private final float width;
    private final float height;
    private final float TILE_SIZE = 64f;
    private final Texture groundTexture;
    private final Body[][] wallBodies;
    private final MapNode[][] nodes;
    private final Array<MapNode> nodesList = new Array<>();
    private final MapGenerator generator;
    private IndexedAStarPathFinder<MapNode> pathFinder;
    private int nodeIndex;

    public MapManager(Game game, String mapName) {
        this.game = game;
        // Load the map
        generator = new MapGenerator(12345);
//        map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
        map = generator.generate(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        float unitScale = 1 / TILE_SIZE;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale, game.getBatch());
        renderer.setView(game.getCamera());

        groundTexture = TextureAssets.get(TextureAssets.GROUND_TEXTURE);
        groundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TiledMapTileLayer wallLayer = getWallLayer();
        width = wallLayer.getWidth();
        height = wallLayer.getHeight();

        heuristic = new EuclideanHeuristic();
        nodes = new MapNode[wallLayer.getWidth()][wallLayer.getHeight()];
        wallBodies = new Body[wallLayer.getWidth()][wallLayer.getHeight()];
        createBodies(wallLayer);
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
                    wallBodies[x][y] = wallBody;
                }
            }
        }
        shape.dispose();
    }

    private void createGroundNodes(TiledMapTileLayer wallLayer) {
        // create the nodes
        nodeIndex = 0;
        for (int y = 0; y < wallLayer.getWidth(); y++) {
            for (int x = 0; x < wallLayer.getHeight(); x++) {
                if (wallLayer.getCell(x, y) == null) {
                    MapNode node = new MapNode(nodeIndex++, x, y);
                    nodesList.add(node);
                    nodes[x][y] = node;
                }
            }
        }

        // create the connections between nodes
        for (MapNode node : nodesList) {
            addConnectionsFromNode(node.x, node.y);
        }
    }

    /**
     * Adds connections from the given node to its neighbours
     *
     * @param x the x coordinate of the node
     * @param y the y coordinate of the node
     */
    private void addConnectionsFromNode(int x, int y) {
        // Horizontal and vertical connections
        addNodeNeighbour(x, y, x - 1, y, 1f);
        addNodeNeighbour(x, y, x + 1, y, 1f);
        addNodeNeighbour(x, y, x, y - 1, 1f);
        addNodeNeighbour(x, y, x, y + 1, 1f);

        // Diagonal connections if possible
        if (isWithinBorder(x - 1, y - 1) && nodes[x - 1][y] != null && nodes[x][y - 1] != null)
            addNodeNeighbour(x, y, x - 1, y - 1, 1.4f);
        if (isWithinBorder(x + 1, y + 1) && nodes[x + 1][y] != null && nodes[x][y + 1] != null)
            addNodeNeighbour(x, y, x + 1, y + 1, 1.4f);
        if (isWithinBorder(x - 1, y + 1) && nodes[x - 1][y] != null && nodes[x][y + 1] != null)
            addNodeNeighbour(x, y, x - 1, y + 1, 1.4f);
        if (isWithinBorder(x + 1, y - 1) && nodes[x + 1][y] != null && nodes[x][y - 1] != null)
            addNodeNeighbour(x, y, x + 1, y - 1, 1.4f);
    }

    /**
     * Adds connections from the neighbouring nodes to the given node
     *
     * @param x x-coordinate of the node
     * @param y y-coordinate of the node
     */
    public void addConnectionsToNode(int x, int y) {
        addNodeNeighbour(x - 1, y, x, y, 1f);
        addNodeNeighbour(x + 1, y, x, y, 1f);
        addNodeNeighbour(x, y - 1, x, y, 1f);
        addNodeNeighbour(x, y + 1, x, y, 1f);

        // Diagonal connections if possible
        if (isWithinBorder(x, y) && nodes[x - 1][y] != null && nodes[x][y - 1] != null)
            addNodeNeighbour(x - 1, y - 1, x, y, 1.4f);
        if (isWithinBorder(x, y) && nodes[x + 1][y] != null && nodes[x][y + 1] != null)
            addNodeNeighbour(x + 1, y + 1, x, y, 1.4f);
        if (isWithinBorder(x, y) && nodes[x - 1][y] != null && nodes[x][y + 1] != null)
            addNodeNeighbour(x - 1, y + 1, x, y, 1.4f);
        if (isWithinBorder(x, y) && nodes[x + 1][y] != null && nodes[x][y - 1] != null)
            addNodeNeighbour(x + 1, y - 1, x, y, 1.4f);
    }

    private void addNodeNeighbour(int nodeX, int nodeY, int neighborX, int neighborY, float cost) {
        if (isWithinBoundary(nodeX, nodeY) && isWithinBoundary(neighborX, neighborY)) {
            MapNode node = nodes[nodeX][nodeY];
            MapNode neighbour = nodes[neighborX][neighborY];
            if (node != null && neighbour != null) {
                node.addConnection(neighbour, cost);
            }
        }
    }

    /**
     * Checks if the given coordinates are within the map excluding the border
     */
    public boolean isWithinBorder(int x, int y) {
        return x >= Constants.MAP_BORDER_LENGTH &&
                x < nodes.length - Constants.MAP_BORDER_LENGTH &&
                y >= Constants.MAP_BORDER_LENGTH &&
                y < nodes[0].length - Constants.MAP_BORDER_LENGTH;
    }

    /**
     * Checks if the given coordinates are within the map
     */
    public boolean isWithinBoundary(int x, int y) {
        return x >= 0 && x < nodes.length && y >= 0 && y < nodes[0].length;
    }

    public boolean isTileWall(int x, int y) {
        if (!isWithinBoundary(x, y)) {
            return true;
        }
        return nodes[x][y] == null;
    }

    /**
     * Checks if the given coordinates are occupied by a structure or a wall and are inside border
     */
    public boolean isTileOccupied(int x, int y) {
        if (!isWithinBorder(x, y)) {
            return true;
        }
        return nodes[x][y] == null || nodes[x][y].hasStructure();
    }

    /**
     * Puts a new structure on the map with its width and height
     *
     * @param x the x coordinate of the bottom left corner of the structure
     * @param y the y coordinate of the bottom left corner of the structure
     */
    public void putNewStructure(Structure structure, int x, int y, int width, int height) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                nodes[i][j].putStructure(structure);
            }
        }
        structures.add(structure);
    }

    /**
     * Removes a structure from the map with its width and height
     *
     * @param x the x coordinate of the bottom left corner of the structure
     * @param y the y coordinate of the bottom left corner of the structure
     */
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

    public void emptySpace(int cX, int cY, int hWidth, int hHeight) {
        TiledMapTileLayer wallLayer = getWallLayer();
        Array<MapNode> newNodes = new Array<>();
        for (int x = cX - hWidth; x < cX + hWidth; x++) {
            for (int y = cY - hHeight; y < cY + hHeight; y++) {
                // If the node is a wall node:
                // 1.Convert it to a ground node
                if (nodes[x][y] == null) {
                    nodes[x][y] = new MapNode(nodeIndex++, x, y);
                    nodesList.add(nodes[x][y]);
                    newNodes.add(nodes[x][y]);
                    // add connections to the new node
                    addConnectionsToNode(x, y);
                }
                // 2.Destroy the wall body and remove the wall cell
                if (wallBodies[x][y] != null) {
                    game.getWorld().destroyBody(wallBodies[x][y]);
                    wallBodies[x][y] = null;
                }
                wallLayer.setCell(x, y, null);
            }
        }

        // To make sure the new nodes are connected to the rest of the map
        for (MapNode node : newNodes) {
            addConnectionsFromNode(node.x, node.y);
        }
        // Reassign the wall shapes
        generator.assignTiles();
        // Need to update the pathfinder with the new nodes
        pathFinder = new IndexedAStarPathFinder<>(this, true);
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

    public DefaultGraphPath<MapNode> findPath(int startX, int startY, int endX, int endY) {
        if (!isWithinBoundary(startX, startY) || !isWithinBoundary(endX, endY)) {
            return null;
        }
        DefaultGraphPath<MapNode> path = new DefaultGraphPath<>();
        MapNode startNode = nodes[startX][startY];
        MapNode endNode = nodes[endX][endY];
        if (startNode == null || endNode == null)
            return null;
        pathFinder.searchNodePath(startNode, endNode, heuristic, path);
        return path;
    }

    public TiledMapTileLayer getWallLayer() {
        return (TiledMapTileLayer) map.getLayers().get(WALL_LAYER);
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }


    public enum CellBodyType {
        WALL, STRUCTURE

    }
}