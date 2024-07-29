package com.mygdx.game.ai.formation;

import com.badlogic.gdx.ai.fma.*;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Updatable;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;


public class FormationManager implements Updatable, Telegraph {
    private final Game game;
    private final Formation<Vector2> formation;
    private final FormationAnchor anchor;
    private final Array<FormationMembership> members = new Array<>();
    private Structure target;
    private boolean isValid = true;


    public FormationManager(Game game, Vector2 start, Structure target) {
        this.game = game;
        this.target = target;
        anchor = new FormationAnchor(game, start, new GridPoint2((int) target.getCenter().x, (int) target.getCenter().y));

        FormationPattern<Vector2> pattern = new SquareFormationPattern();
        FormationMotionModerator<Vector2> motionModerator = new AnchorModerator(members);
        formation = new Formation<>(anchor, pattern, new FreeSlotAssignmentStrategy<>(), motionModerator);
    }

    public void addMember(FormationMembership membership) {
        if (!isValid)
            throw new IllegalStateException("This formation doesn't exist");
        membership.registerTelegraph(this);
        members.add(membership);
        formation.addMember(membership);
    }

    public Vector2 getPosition() {
        return anchor.getPosition();
    }

    private void breakFormation() {
        for (FormationMembership membership : members) {
            MessageManager.getInstance().dispatchMessage(
                    null,
                    membership.getOwner().getTelegraph(),
                    MessageType.BREAK_FORMATION.ordinal(),
                    target
            );
        }
        dispose();
    }

    @Override
    public void update(float delta) {
        if (!isValid)
            throw new IllegalStateException("This formation doesn't exist");
        anchor.update(delta);
        formation.updateSlots();
        if (anchor.distanceToTarget() < Constants.BREAK_FORMATION_RANGE) {
            breakFormation();
            return;
        }

        // Debug rendering
        for (int i = 0; i < members.size; i++) {
            SlotAssignment<Vector2> assignment = formation.getSlotAssignmentAt(i);
            if (assignment != null) {
                Location<Vector2> slotPosition = assignment.member.getTargetLocation();
                Debug.drawPoint("slot" + i, slotPosition.getPosition());
            }
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public Array<FormationMembership> getMembers() {
        return members;
    }

    public void dispose() {
        isValid = false;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == MessageType.BREAK_FORMATION.ordinal()) {
            target = (Structure) msg.extraInfo;
            breakFormation();
            return true;
        }
        return false;
    }
}
