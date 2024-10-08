package com.mygdx.game.entities.structures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Renderable;
import com.mygdx.game.Updatable;
import com.mygdx.game.world.Game;

public class StructureBuilder implements Updatable, Renderable {
    private final Game game;
    private boolean inBuildMode = false;
    private Structure.Builder currentStructure;

    public StructureBuilder(Game game) {
        this.game = game;
    }

    private GridPoint2 getGridMousePosition() {
        Vector3 mousePos = game.unproject(Gdx.input.getX(), Gdx.input.getY());
        mousePos.x = Math.round(mousePos.x);
        mousePos.y = Math.round(mousePos.y);
        return new GridPoint2((int) mousePos.x, (int) mousePos.y);
    }

    public void setBuild(Structure.Builder builder) {
        currentStructure = builder;
        GridPoint2 mousePos = getGridMousePosition();
        builder.setGhostPosition(mousePos.x, mousePos.y);
        inBuildMode = true;
    }

    @Override
    public void update(float deltaTime) {
        if (inBuildMode) {
            GridPoint2 mousePos = getGridMousePosition();

            currentStructure.setBounds(mousePos.x, mousePos.y);
            Bounds bounds = currentStructure.getBounds();
            if (game.areTilesOccupied(bounds.x, bounds.y, bounds.width, bounds.height)) {
                currentStructure.setGhostValid(false);
                if (Gdx.input.justTouched()) {
                    System.out.println("Cannot build here");
                    inBuildMode = false;
                    currentStructure = null;
                }
            } else {
                currentStructure.setGhostValid(true);
                if (Gdx.input.justTouched()) {
                    Structure structure = currentStructure.build();
                    game.entities.addStructure(structure);
                    inBuildMode = false;
                    currentStructure = null;
                }
            }
            if (currentStructure != null) {
                currentStructure.setGhostPosition(mousePos.x, mousePos.y);
            }
        } else { // Quick builds
            // TODO: in input processor
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                inBuildMode = true;
                GridPoint2 mousePos = getGridMousePosition();
                currentStructure = Structures.basicTurret(game, mousePos.x, mousePos.y);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                inBuildMode = true;
                GridPoint2 mousePos = getGridMousePosition();
                currentStructure = Structures.burstTurret(game, mousePos.x, mousePos.y);
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
