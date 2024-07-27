package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ai.Agent;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class FormationMember extends Agent implements com.badlogic.gdx.ai.fma.FormationMember<Vector2> {

    private static final float EPSILON = 0.05f;
    private final Arrive<Vector2> arriveTarget;
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private boolean onPath = false;
    private GameLocation slotLocation;
    private GridPoint2 lastPathTarget;
    private FollowPath<Vector2, LinePath.LinePathParam> followPath;


    public FormationMember(Game game) {
        super(game);
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

    private LinePath<Vector2> convertToLinePath(GraphPath<MapNode> graphPath) {
        Array<Vector2> waypoints = new Array<>(graphPath.getCount());
        for (MapNode node : graphPath) {
            waypoints.add(new Vector2(node.x, node.y));
        }
        return new LinePath<>(waypoints, true);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

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
                followPath = new FollowPath<>(this, convertToLinePath(graphPath))
                        .setPathOffset(0.5f)
                        .setTimeToTarget(0.25f);
            } else {
                followPath.setPath(convertToLinePath(graphPath));
            }
        } else { // follow the path
            followPath.calculateSteering(steering);
        }
        Debug.log(this + "path", curPoint + " " + onPath + ": " + lastPathTarget);

        body.applyForceToCenter(steering.linear, true);


//        steering.setZero();
//        obstacleAvoidance.calculateSteering(steering);
////        System.out.println(steering.linear);
//
//        if (!steering.linear.isZero(EPSILON)) {
//            Vector2 force = steering.linear.cpy();
//            steering.setZero();
//            force.mulAdd(steering.linear, 0.5f);
//            body.applyForceToCenter(force, true);
//        } else {
//            steering.setZero();
//            arriveSB.calculateSteering(steering);
//            if (!steering.linear.isZero(EPSILON)) {
//                body.applyForceToCenter(steering.linear, true);
////            Debug.log("Steering linear" + this, steering.linear);
//            }
//        }

    }

    @Override
    public void render() {
        super.render();

//        if (rayConfig != null) {
//            for (Ray<Vector2> ray : rayConfig.getRays()) {
//                Debug.drawLine("" + ray, ray.start, ray.end);
//            }
//            Debug.log("Ray count: ", rayConfig.getRays().length);
//        }
    }

    @Override
    public Location<Vector2> getTargetLocation() {
        return slotLocation;
    }
}