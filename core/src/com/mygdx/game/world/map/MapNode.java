package com.mygdx.game.world.map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entities.structures.Structure;

public class MapNode {
    public final int x;
    public final int y;
    private final int index;
    private final Array<Connection<MapNode>> connections = new Array<>();
    private Structure structure;

    public MapNode(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public void putStructure(Structure structure) {
        if (this.hasStructure() && structure != null)
            throw new IllegalStateException("(" + x + ", " + y + ") already has a structure.");
        this.structure = structure;
    }

    public boolean hasStructure() {
        return structure != null;
    }

    public Structure getStructure() {
        return structure;
    }

    public int getIndex() {
        return index;
    }

    public boolean doesConnectionExist(MapNode neighbor) {
        for (Connection<MapNode> connection : connections) {
            if (connection.getToNode() == neighbor) {
                return true;
            }
        }
        return false;
    }

    public void addConnection(MapNode node, float cost) {
        if (node != null) {
            if (!doesConnectionExist(node)) {
                connections.add(new NodeConnection(cost, this, node));
            }
        }
    }

    public void removeConnection(MapNode node) {
        for (Array.ArrayIterator<Connection<MapNode>> iterator = connections.iterator(); iterator.hasNext(); ) {
            Connection<MapNode> connection = iterator.next();
            if (connection.getToNode() == node) {
                iterator.remove();
            }
        }
    }

    public Array<Connection<MapNode>> getConnections() {
        return connections;
    }
}
