package com.mygdx.game.utils;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.world.map.MapNode;

public class Utils {

    public static LinePath<Vector2> convertToLinePath(GraphPath<MapNode> graphPath) throws SingleNodePathException {
        Array<Vector2> vertices = new Array<>();
        for (MapNode node : graphPath) {
            vertices.add(new Vector2(node.x + .5f, node.y + .5f));
        }
        if (vertices.size < 2) {
            throw new SingleNodePathException();
        }
        return new LinePath<>(vertices, true);
    }

    public static class SingleNodePathException extends Exception {
    }
}
