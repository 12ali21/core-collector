package com.mygdx.game.ai.agents;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.utils.Bodies;
import com.mygdx.game.utils.Utils;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

import static com.mygdx.game.utils.Utils.convertToLinePath;

public class BotAgent extends Agent {

    FollowPath<Vector2, LinePath.LinePathParam> followPath;

    public BotAgent(Game game, Vector2 position) {
        super(game, Bodies.createBotBody(game, position));
        setMaxAngularSpeed(50);
        setMaxAngularAcceleration(15f);
        setZeroLinearSpeedThreshold(0.01f);
        look.setDecelerationRadius(MathUtils.PI / 2f);
    }

    @Override
    public void update() {
        super.update();
        if (followPath != null) {
            followPath.calculateSteering(steering);
            applySteering();
        }
    }

    public void setMoveToTarget(float x, float y) {
        DefaultGraphPath<MapNode> graphPath = game.map.findPath(
                (int) getPosition().x,
                (int) getPosition().y,
                (int) x,
                (int) y
        );
        if (graphPath == null) {
            return;
        }
        LinePath<Vector2> path;
        try {
            path = convertToLinePath(graphPath);
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
}
