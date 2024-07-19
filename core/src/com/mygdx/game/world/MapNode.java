package com.mygdx.game.world;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

public class MapNode {
    private final int index;
    public final int x;
    public final int y;

    private final Array<Connection<MapNode>> connections = new Array<>();

    public MapNode(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public int getIndex() {
        return index;
    }

    public void addConnection(MapNode node, float cost) {
        if (node != null) {
            connections.add(new NodeConnection(cost, this, node));
        }
    }

    public Array<Connection<MapNode>> getConnections() {
        return connections;
    }
}
