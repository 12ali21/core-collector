package com.mygdx.game.ai.agents;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ai.GameLocation;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.entities.structures.turret.Turret;
import com.mygdx.game.utils.Bodies;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

import static com.mygdx.game.utils.Utils.convertToLinePath;

public class BotAgent extends Agent {

    private static final float MOVING_TARGET_TOLERANCE = 0.1f;
    private static final float MOVING_PILOT_TOLERANCE = 0.5f;

    private final Vector2 movingToTarget = new Vector2();
    private FollowPath<Vector2, LinePath.LinePathParam> followPath;
    private final StateMachine<BotAgent, BotState> stateMachine;
    private Turret pilotTurret;
    private boolean seated = false;
    private BotState nextState;
    private GameLocation seatLocation;


    public BotAgent(Game game, Vector2 position) {
        super(game, Bodies.createBotBody(game, position));
        setMaxAngularSpeed(50);
        setMaxAngularAcceleration(15f);
        setZeroLinearSpeedThreshold(0.01f);
        look.setDecelerationRadius(MathUtils.PI / 2f);

        stateMachine = new DefaultStateMachine<>(this, BotState.IDLE);
    }

    public boolean followPath() {
        if (followPath != null) {
            followPath.calculateSteering(steering);
            applySteering();
            if (nextState == BotState.PILOT)
                return !(getPosition().dst(movingToTarget) > MOVING_PILOT_TOLERANCE);
            else
                return !(getPosition().dst(movingToTarget) > MOVING_TARGET_TOLERANCE);
        }
        return true;
    }

    @Override
    public void update() {
        stateMachine.update();
        Debug.log("bot " + this + " state", stateMachine.getCurrentState());
        super.update();
    }

    public void setMoveToTarget(float x, float y) {
        stateMachine.changeState(BotState.MOVING);
        movingToTarget.set(x, y);
        nextState = BotState.IDLE;

        Structure s = game.map.getStructureAt((int) x, (int) y);
        if (s instanceof Turret) {
            pilotTurret = (Turret) s;
            movingToTarget.set(s.getCenter());
            nextState = BotState.PILOT;
        }
        makePath(movingToTarget);
    }

    private void makePath(Vector2 target) {

        DefaultGraphPath<MapNode> graphPath = game.map.findPath(
                (int) getPosition().x,
                (int) getPosition().y,
                (int) target.x,
                (int) target.y
        );
        if (graphPath == null) {
            return;
        }
        LinePath<Vector2> path;
        try {
            path = convertToLinePath(graphPath, target);
        } catch (Utils.SingleNodePathException e) {
            return; // already there
        }
        if (followPath == null) {
            followPath = new FollowPath<>(this, path, 1.5f)
                    .setDecelerationRadius(1f)
                    .setTimeToTarget(2f);
        } else {
            followPath.setPath(path);
        }

    }

    // Gets called from the piloting turret
    public void setSeatLocation(Vector2 seatPos, float rotation) {
        seatLocation = new GameLocation(seatPos, rotation);
    }

    public boolean isSeated() {
        return seated;
    }

    private enum BotState implements State<BotAgent> {
        IDLE() {

        },
        MOVING() {
            @Override
            public void update(BotAgent entity) {
                if (entity.followPath()) {
                    entity.stateMachine.changeState(entity.nextState);
                }
            }
        },
        PILOT() {
            @Override
            public void enter(BotAgent entity) {
                if (entity.pilotTurret != null && entity.pilotTurret.isAlive()) {
                    entity.pilotTurret.setPilot(entity);
                } else {
                    entity.stateMachine.changeState(IDLE);
                }
            }

            @Override
            public void update(BotAgent entity) {
                if (entity.pilotTurret != null && entity.pilotTurret.isAlive()) {
                    entity.setTransform(entity.seatLocation.getPosition(), entity.seatLocation.getOrientation());
                    entity.seated = true;
                } else {
                    entity.stateMachine.changeState(IDLE);
                    entity.seated = false;
                }
            }

            @Override
            public void exit(BotAgent entity) {
                if (entity.pilotTurret != null) {
                    entity.pilotTurret.setPilot(null);
                    entity.pilotTurret = null;
                }
                System.out.println("exited");
            }
        };

        @Override
        public void enter(BotAgent entity) {

        }

        @Override
        public void update(BotAgent entity) {

        }

        @Override
        public void exit(BotAgent entity) {

        }

        @Override
        public boolean onMessage(BotAgent entity, Telegram telegram) {
            return false;
        }
    }
}
