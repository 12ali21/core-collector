package com.mygdx.game.world;

import com.badlogic.gdx.ai.pfa.Heuristic;

public class EuclideanHeuristic implements Heuristic<MapNode> {

    @Override
    public float estimate(MapNode node, MapNode endNode) {
        return (float) Math.sqrt(Math.pow(endNode.x - node.x, 2) + Math.pow(endNode.y - node.y, 2));
    }
}
