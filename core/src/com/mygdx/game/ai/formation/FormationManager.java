package com.mygdx.game.ai.formation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fma.*;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;
import com.mygdx.game.world.map.MapNode;


public class FormationManager {
    private final Game game;
    Formation<Vector2> formation;
    Array<FormationMember> members;
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

        followPath = new FollowPath<>(anchor, path, 3f);
        followPath.setPredictionTime(0);
        followPath.setDecelerationRadius(.5f);

        anchor.setSteeringBehavior(followPath);


        members = new Array<>();
        members.add(createFormationMember());
        members.add(createFormationMember());
        members.add(createFormationMember());
        members.add(createFormationMember());
        members.add(createFormationMember());
        members.add(createFormationMember());
        members.add(createFormationMember());

        FormationPattern<Vector2> pattern = new SquareFormationPattern();
        FormationMotionModerator<Vector2> motionModerator = new AnchorModerator(members);
        formation = new Formation<>(anchor, pattern, new FreeSlotAssignmentStrategy<>(), motionModerator);

        for (FormationMember member : members) {
            formation.addMember(member);
        }
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

        for (FormationMember member : members) {
            member.update(delta);
        }
    }

    public void render() {
        anchor.render();
        for (FormationMember member : members) {
            member.render();
        }

        for (int i = 0; i < members.size; i++) {
            SlotAssignment<Vector2> assignment = formation.getSlotAssignmentAt(i);
            if (assignment != null) {
                Location<Vector2> slotPosition = assignment.member.getTargetLocation();
                Debug.drawPoint("slot" + i, slotPosition.getPosition());
            }
        }
    }

    private FormationMember createFormationMember() {
        return new FormationMember(game);
    }
}
