package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.entities.Drawable;
import com.mygdx.game.entities.Structure;
import com.mygdx.game.entities.Updatable;
import com.mygdx.game.entities.World;
import com.mygdx.game.entities.turret.Turrets;

public class StructureBuilder implements Updatable, Drawable {
    private final World world;
    private boolean inBuildMode = false;
    private Structure.Builder currentStructure;

    public StructureBuilder(World world) {
        this.world = world;
    }

    private GridPoint2 getGridMousePosition() {
        Vector3 mousePos = world.unproject(Gdx.input.getX(), Gdx.input.getY());
        mousePos.x = Math.round(mousePos.x);
        mousePos.y = Math.round(mousePos.y);
        return new GridPoint2((int) mousePos.x, (int) mousePos.y);
    }

    @Override
    public void update(float deltaTime) {
        if (!inBuildMode) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                inBuildMode = true;
                GridPoint2 mousePos = getGridMousePosition();
                currentStructure = Turrets.basicTurret(world, mousePos.x, mousePos.y);
            }
        } else {
            GridPoint2 mousePos = getGridMousePosition();
            if (Gdx.input.isTouched()) {
                currentStructure.setBounds(mousePos.x, mousePos.y, 2, 2);
                Structure structure = currentStructure.build();
                world.addStructure(structure);
                inBuildMode = false;
            } else {
                currentStructure.setGhostPosition(mousePos.x, mousePos.y);
            }
        }

    }

    @Override
    public void render() {
        if (inBuildMode) {
            currentStructure.renderGhost();
        }
    }
}
