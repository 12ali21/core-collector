package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.ai.agents.EnemyAgent;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class FormationMembership implements com.badlogic.gdx.ai.fma.FormationMember<Vector2> {

    private final GameLocation slotLocation;
    private final Game game;
    private final EnemyAgent owner;
    private final Arrive<Vector2> arriveTarget;
    private FormationManager currentFormation;
    private FollowPath<Vector2, LinePath.LinePathParam> followPath;
    private boolean onPath = false;
    private GridPoint2 lastPathTarget;


    public FormationMembership(Game game, EnemyAgent owner) {
        this.game = game;
        this.owner = owner;
        this.slotLocation = new GameLocation();

        arriveTarget = new Arrive<>(owner, this.getTargetLocation()) // for arriving at slot position
                .setTimeToTarget(2f)
                .setDecelerationRadius(0.1f)
                .setLimiter(owner);
    }

    private boolean canReachDiagonal(int x1, int y1, int x2, int y2) {
        if (x2 == x1 + 1 && y2 == y1 + 1) {
            return !game.map.isTileOccupied(x1 + 1, y1) && !game.map.isTileOccupied(x1, y1 + 1);
        } else if (x2 == x1 + 1 && y2 == y1 - 1) {
            return !game.map.isTileOccupied(x1 + 1, y1) && !game.map.isTileOccupied(x1, y1 - 1);
        } else if (x2 == x1 - 1 && y2 == y1 + 1) {
            return !game.map.isTileOccupied(x1 - 1, y1) && !game.map.isTileOccupied(x1, y1 + 1);
        } else if (x2 == x1 - 1 && y2 == y1 - 1) {
            return !game.map.isTileOccupied(x1 - 1, y1) && !game.map.isTileOccupied(x1, y1 - 1);
        } else {
            return false;
        }
    }

    public void calculateSteering(SteeringAcceleration<Vector2> steering) {
        Vector2 tmp = owner.getPosition();
        GridPoint2 curPoint = new GridPoint2((int) tmp.x, (int) tmp.y);
        tmp = getTargetLocation().getPosition();
        GridPoint2 targetPoint = new GridPoint2((int) tmp.x, (int) tmp.y);
        // if the target is adjacent or diagonally adjacent arrive at it or the agent is outside the borders
        if (targetPoint.dst(curPoint) <= 1f + 0.05f
                || canReachDiagonal(curPoint.x, curPoint.y, targetPoint.x, targetPoint.y)
                || !game.map.isWithinBoundary(curPoint.x, curPoint.y)) {
            onPath = false;
            arriveTarget.calculateSteering(steering);
        } else if (!onPath || (lastPathTarget != null && !lastPathTarget.equals(targetPoint))) { // if not already following a path or the target changed
            //find the path towards its corresponding tile until adjacency
            GraphPath<MapNode> graphPath = game.map.findPath(curPoint.x, curPoint.y, targetPoint.x, targetPoint.y);
            if (graphPath == null) {
                return; // wait for space
            }
            onPath = true;
            lastPathTarget = targetPoint;
            if (followPath == null) {
                try {
                    followPath = new FollowPath<>(owner, Utils.convertToLinePath(graphPath))
                            .setPathOffset(0.5f)
                            .setTimeToTarget(0.25f);
                } catch (Utils.SingleNodePathException e) {
                    // is already handled in this method
                }
            } else {
                try {
                    followPath.setPath(Utils.convertToLinePath(graphPath));
                } catch (Utils.SingleNodePathException e) {
                    // is already handled in this method
                }
            }
        } else { // follow the path
            followPath.calculateSteering(steering);
        }
    }

    @Override
    public Location<Vector2> getTargetLocation() {
        return slotLocation;
    }

    public EnemyAgent getOwner() {
        return owner;
    }

    public void registerTelegraph(FormationManager formationManager) {
        this.currentFormation = formationManager;
    }

    public boolean isFormationValid() {
        return currentFormation.isValid();
    }

    public void requestFormationBreak(Structure owner) {
        MessageManager.getInstance().dispatchMessage(
                null,
                currentFormation,
                MessageType.BREAK_FORMATION.ordinal(),
                owner
        );
    }
}