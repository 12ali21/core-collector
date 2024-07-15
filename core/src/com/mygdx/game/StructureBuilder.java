package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.entities.Drawable;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.Updatable;
import com.mygdx.game.entities.turret.BasicTurret;
import com.mygdx.game.entities.turret.Turret;

public class StructureBuilder implements Updatable, Drawable {
    private final World world;
    private boolean inBuildMode = false;
    private Sprite currentStructure;

    public StructureBuilder(World world) {
        this.world = world;
    }

    private Vector2 getWorldMousePosition() {
        Vector3 mousePos = world.unproject(Gdx.input.getX(), Gdx.input.getY());
        mousePos.x = Math.round(mousePos.x);
        mousePos.y = Math.round(mousePos.y);
        return new Vector2(mousePos.x, mousePos.y);
    }

    @Override
    public void update(float deltaTime) {
        if (!inBuildMode) {
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
                inBuildMode = true;
                Entity entity = new BasicTurret(world, Utils.getMousePosition());
                currentStructure = entity.getGhost();
            }
        } else {
            Vector2 mousePos = getWorldMousePosition();
            if (Gdx.input.isTouched()) {
                world.addEntity(new BasicTurret(world, mousePos));
                inBuildMode = false;
            } else {
                currentStructure.setOriginBasedPosition(mousePos.x, mousePos.y);
            }
        }

    }

    @Override
    public void render() {
        if (inBuildMode) {
            currentStructure.draw(world.getBatch());
        }
    }
}
