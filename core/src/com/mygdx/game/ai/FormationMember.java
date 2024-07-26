package com.mygdx.game.ai;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.limiters.LinearLimiter;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;

public class FormationMember extends Agent implements com.badlogic.gdx.ai.fma.FormationMember<Vector2> {

    private static final float EPSILON = 0.05f;
    private final RayConfigurationBase<Vector2> rayConfig;
    private final RaycastObstacleAvoidance<Vector2> obstacleAvoidance;
    private final Arrive<Vector2> arriveSB;
    private final SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
    private GameLocation target;

    public FormationMember(Game game) {
        super(game);
        this.target = new GameLocation();

        RaycastCollisionDetector<Vector2> raycastCollisionDetector = new Box2dRaycastCollisionDetector(game.getWorld());
        rayConfig = new CentralRayWithWhiskersConfiguration<>(
                this,
                1,
                0.5f,
                35 * MathUtils.degreesToRadians
        );


        obstacleAvoidance = new RaycastObstacleAvoidance<>(
                this,
                rayConfig,
                raycastCollisionDetector,
                22f)
                .setLimiter(new LinearLimiter(2, 20));

        arriveSB = new Arrive<>(this, this.getTargetLocation())
                .setLimiter(new LinearLimiter(2, 5))
                .setTimeToTarget(0.5f)
                .setArrivalTolerance(0.001f)
                .setDecelerationRadius(5);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        steering.setZero();
        obstacleAvoidance.calculateSteering(steering);
//        System.out.println(steering.linear);

        body.applyForceToCenter(steering.linear, true);

        steering.setZero();
        arriveSB.calculateSteering(steering);
        if (!steering.linear.isZero(EPSILON)) {
            body.applyForceToCenter(steering.linear, true);
//            Debug.log("Steering linear" + this, steering.linear);
        }
    }

    @Override
    public void render() {
        super.render();

        if (rayConfig != null) {
            for (Ray<Vector2> ray : rayConfig.getRays()) {
                Debug.drawLine("" + ray, ray.start, ray.end);
            }
            Debug.log("Ray count: ", rayConfig.getRays().length);
        }
    }

    @Override
    public Location<Vector2> getTargetLocation() {
        return target;
    }
}