package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;

public class ScreenInputProcessor extends InputAdapter implements Updatable {


    private final Vector3 cameraMovement = new Vector3();
    private float zoom = 1f;
    private boolean FOVChanged = false;

    @Override
    public boolean scrolled(float amountX, float amountY) {
        zoom += amountY * Constants.ZOOM_CHANGE_SPEED;
        zoom = MathUtils.clamp(zoom, Constants.ZOOM_LIMIT_LOW, Constants.ZOOM_LIMIT_HIGH);
        FOVChanged = true;
        return true;
    }

    @Override
    public void update(float delta) {
        cameraMovement.setZero();
        float movement = Constants.PAN_SPEED * delta * zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            movement *= 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cameraMovement.y += movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            cameraMovement.y -= movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cameraMovement.x -= movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            cameraMovement.x += movement;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            Debug.setDebugging(!Debug.isDebugging());
        }
    }

    public boolean isFOVChanged() {
        return FOVChanged;
    }

    public float getZoom() {
        FOVChanged = false;
        return zoom;
    }

    public Vector3 getCameraMovement() {
        return cameraMovement;
    }
}
