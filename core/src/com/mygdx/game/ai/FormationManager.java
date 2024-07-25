package com.mygdx.game.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fma.Formation;
import com.badlogic.gdx.ai.fma.FormationMotionModerator;
import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.fma.FreeSlotAssignmentStrategy;
import com.badlogic.gdx.ai.fma.patterns.OffensiveCircleFormationPattern;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.limiters.AngularLimiter;
import com.badlogic.gdx.ai.steer.limiters.LinearLimiter;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;

public class FormationManager {
    private final Game game;
    Formation<Vector2> formation;
    FormationMember memberA;
    FormationMember memberB;
    FormationMember memberC;
    FormationAnchor anchor;
    FollowPath<Vector2, LinePath.LinePathParam> followPath;


    public FormationManager(Game game) {
        this.game = game;
        anchor = new FormationAnchor();
        anchor.setMaxLinearSpeed(2f);
        anchor.setMaxLinearAcceleration(20f);
        anchor.getPosition().set(1.5f, 1.5f);

        DefaultGraphPath<MapNode> testGraphPath = game.map.findPath(1, 1, 25, 15);
        Array<Vector2> vertices = new Array<>();
        for (MapNode node : testGraphPath) {
            vertices.add(new Vector2(node.x + .5f, node.y + .5f));
        }
        LinePath<Vector2> path = new LinePath<>(vertices, true);

        followPath = new FollowPath<>(anchor, path, 1f);
        followPath.setPredictionTime(0.1f);
        followPath.setDecelerationRadius(.5f);

        anchor.setSteeringBehavior(followPath);
        memberA = createFormationMember();
        memberB = createFormationMember();
        memberC = createFormationMember();

        Array<FormationMember> members = new Array<>();
        members.add(memberA);
        members.add(memberB);
        members.add(memberC);

        FormationPattern<Vector2> pattern = new OffensiveCircleFormationPattern<>(1);
        FormationMotionModerator<Vector2> motionModerator = new AnchorModerator(members);
        formation = new Formation<>(anchor, pattern, new FreeSlotAssignmentStrategy<>(), motionModerator);


        formation.addMember(memberA);
        formation.addMember(memberB);
        formation.addMember(memberC);

    }

    private LinePath<Vector2> calculatePath(int x, int y) {
        Vector2 cur = anchor.getPosition();
        DefaultGraphPath<MapNode> testGraphPath = game.map.findPath((int) cur.x, (int) cur.y, x, y);
        Array<Vector2> vertices = new Array<>();
        for (MapNode node : testGraphPath) {
            vertices.add(new Vector2(node.x + .5f, node.y + .5f));
        }
        return new LinePath<>(vertices, true);
    }

    public void update(float delta) {
        if (Gdx.input.justTouched()) {
            Vector3 pos = game.unproject(Gdx.input.getX(), Gdx.input.getY());
            followPath.setPath(calculatePath((int) pos.x, (int) pos.y));
            anchor.setSteeringBehavior(followPath);
        }

        anchor.update(delta);
        formation.updateSlots();

        memberA.update(delta);
        memberB.update(delta);
        memberC.update(delta);
    }

    public void render() {
        memberA.render();
        memberB.render();
        memberC.render();
    }

    private FormationMember createFormationMember() {

        RaycastCollisionDetector<Vector2> raycastCollisionDetector = new Box2dRaycastCollisionDetector(game.getWorld());

        return new FormationMember(game) {

            boolean instantiated = false;
            RayConfigurationBase<Vector2> rayConfig;

            @Override
            public void update(float delta) {
                if (!instantiated) {
                    rayConfig = new CentralRayWithWhiskersConfiguration<>(
                            this,
                            0.1f,
                            0.7f,
                            35 * MathUtils.degreesToRadians
                    );

                    Arrive<Vector2> arriveSB = new Arrive<>(this, this.getTargetLocation())
                            .setLimiter(new LinearLimiter(350, 100))
                            .setTimeToTarget(0.1f)
                            .setArrivalTolerance(0.001f)
                            .setDecelerationRadius(40);
                    instantiated = true;

                    LookWhereYouAreGoing<Vector2> lookWhereYouAreGoingSB = new LookWhereYouAreGoing<>(this)
                            .setLimiter(new AngularLimiter(1, 6f))
                            .setTimeToTarget(0.1f)
                            .setAlignTolerance(0)
                            .setDecelerationRadius(MathUtils.PI / 6f);

                    RaycastObstacleAvoidance<Vector2> raycastObstacleAvoidanceSB = new RaycastObstacleAvoidance<>(
                            this,
                            rayConfig,
                            raycastCollisionDetector,
                            0.001f);

                    BlendedSteering<Vector2> p1 = new BlendedSteering<>(this)
                            .add(raycastObstacleAvoidanceSB, 1f);

                    BlendedSteering<Vector2> p2 = new BlendedSteering<>(this)
                            .add(arriveSB, 1f);

                    PrioritySteering<Vector2> steering = new PrioritySteering<>(this, 0.001f)
                            .add(p1)
                            .add(p2);

                    this.setSteeringBehavior(steering);
                }
                if (rayConfig != null) {
                    for (Ray<Vector2> ray : rayConfig.getRays()) {
                        Debug.drawLine("" + ray, ray.start, ray.end);
                    }
                    Debug.log("Ray count: ", rayConfig.getRays().length);
                }
                super.update(delta);
            }
        };
    }
}
