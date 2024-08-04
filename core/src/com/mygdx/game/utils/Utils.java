package com.mygdx.game.utils;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.world.map.MapNode;

public class Utils {
    private static final Vector2 tmp = new Vector2();

    private static Array<Vector2> getVertices(GraphPath<MapNode> graphPath) {
        Array<Vector2> vertices = new Array<>();
        for (MapNode node : graphPath) {
            vertices.add(new Vector2(node.x + .5f, node.y + .5f));
        }
        return vertices;
    }

    /**
     * Converts graph path from the nodes of the map, to a line path for use with FollowPath class
     *
     * @throws SingleNodePathException if the nodes of the graph are less than 2
     */
    public static LinePath<Vector2> convertToLinePath(GraphPath<MapNode> graphPath) throws SingleNodePathException {
        Array<Vector2> vertices = getVertices(graphPath);
        if (vertices.size < 2) {
            throw new SingleNodePathException();
        }
        return new LinePath<>(vertices, true);
    }

    /**
     * Converts graph path from the nodes of the map plus the target node, to a line path for use with FollowPath class
     *
     * @throws SingleNodePathException if the nodes of the graph are less than 2
     */
    public static LinePath<Vector2> convertToLinePath(GraphPath<MapNode> graphPath, Vector2 target) throws SingleNodePathException {
        Array<Vector2> vertices = getVertices(graphPath);
        vertices.add(new Vector2(target));
        if (vertices.size < 2) {
            throw new SingleNodePathException();
        }
        return new LinePath<>(vertices, true);
    }

    public static class SingleNodePathException extends Exception {
    }
}
