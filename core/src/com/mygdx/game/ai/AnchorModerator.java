package com.mygdx.game.ai;

import com.badlogic.gdx.ai.fma.FormationMotionModerator;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

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
}
