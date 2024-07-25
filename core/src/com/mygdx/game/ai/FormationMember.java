package com.mygdx.game.ai;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.world.Game;

public class FormationMember extends Agent implements com.badlogic.gdx.ai.fma.FormationMember<Vector2> {

    private GameLocation target;

    public FormationMember(Game game) {
        super(game);
        this.target = new GameLocation();
    }

    @Override
    public Location<Vector2> getTargetLocation() {
        return target;
    }
}