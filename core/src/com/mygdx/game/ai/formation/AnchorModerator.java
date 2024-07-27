package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.fma.FormationMotionModerator;
import com.badlogic.gdx.ai.fma.FormationPattern;
import com.badlogic.gdx.ai.fma.SlotAssignment;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ai.GameLocation;

public class AnchorModerator extends FormationMotionModerator<Vector2> {
    public static final float OFFSET = 0.2f;
    Array<FormationMember> members;
    private Vector2 avgPos = new Vector2();
    private Vector2 avgVel = new Vector2();

    public AnchorModerator(Array<FormationMember> members) {
        this.members = members;
    }

    private void calculateAvgPosition() {
        avgPos.setZero();
        for (FormationMember member : members) {
            avgPos.add(member.getPosition());
        }
        avgPos.scl(1f / members.size);
    }

    private void calculateAvgVelocity() {
        avgVel.setZero();
        for (FormationMember member : members) {
            avgVel.add(member.getLinearVelocity());
        }
        avgVel.scl(1f / members.size);
    }

    @Override
    public void updateAnchorPoint(Location<Vector2> anchor) {
        calculateAvgPosition();
        calculateAvgVelocity();
//        anchor.getPosition().set(avgPos.mulAdd(avgVel, OFFSET));
    }

    @Override
    public Location<Vector2> calculateDriftOffset(Location<Vector2> centerOfMass, Array<SlotAssignment<Vector2>> slotAssignments, FormationPattern<Vector2> pattern) {
        return new GameLocation();
    }
}
