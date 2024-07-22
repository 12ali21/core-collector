package com.mygdx.game.world.map;

import com.badlogic.gdx.ai.pfa.Connection;

public class NodeConnection implements Connection<MapNode> {
    private final float cost;

    private final MapNode fromNode;
    private final MapNode toNode;

    public NodeConnection(float cost, MapNode fromNode, MapNode toNode) {
        this.cost = cost;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public MapNode getFromNode() {
        return fromNode;
    }

    @Override
    public MapNode getToNode() {
        return toNode;
    }
}
