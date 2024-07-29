package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.ai.agents.Agent;
import com.mygdx.game.ai.agents.EnemyAgent;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class FormationMembership extends Agent implements com.badlogic.gdx.ai.fma.FormationMember<Vector2> {

    private static final float EPSILON = 0.05f;
    private final Arrive<Vector2> arriveTarget;
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private final GameLocation slotLocation;
    private final EnemyAgent owner;
    private boolean onPath = false;
    private GridPoint2 lastPathTarget;
    private FollowPath<Vector2, LinePath.LinePathParam> followPath;
    private Telegraph formationTelegraph;


    public FormationMembership(Game game, Body body, EnemyAgent owner) {
        super(game, body);
        this.owner = owner;
        this.slotLocation = new GameLocation();

        arriveTarget = new Arrive<>(this, this.getTargetLocation())
                .setTimeToTarget(0.2f)
                .setArrivalTolerance(0.001f)
                .setDecelerationRadius(1.2f);
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

    @Override
    public void update() {
        super.update();

        Vector2 tmp = getPosition();
        GridPoint2 curPoint = new GridPoint2((int) tmp.x, (int) tmp.y);
        tmp = getTargetLocation().getPosition();
        GridPoint2 targetPoint = new GridPoint2((int) tmp.x, (int) tmp.y);
        // if the target is adjacent or diagonally adjacent arrive at it
        if (targetPoint.dst(curPoint) <= 1f + EPSILON
                || canReachDiagonal(curPoint.x, curPoint.y, targetPoint.x, targetPoint.y)) {
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
                    followPath = new FollowPath<>(this, Utils.convertToLinePath(graphPath))
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

        body.applyForceToCenter(steering.linear, true);

    }

    @Override
    public Location<Vector2> getTargetLocation() {
        return slotLocation;
    }

    public EnemyAgent getOwner() {
        return owner;
    }

    public void registerTelegraph(Telegraph formationManager) {
        formationTelegraph = formationManager;
    }

    public void requestFormationBreak(Structure owner) {
        MessageManager.getInstance().dispatchMessage(
                null,
                formationTelegraph,
                MessageType.BREAK_FORMATION.ordinal(),
                owner
        );
    }
}