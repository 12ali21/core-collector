package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Constants;

public class ScreenInputProcessor extends InputAdapter implements Updatable {


    private final Vector2 cameraPosition = new Vector2(0, 0);
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
        float movement = Constants.PAN_SPEED * delta * 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            movement *= 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            cameraPosition.y += movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            cameraPosition.y -= movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            cameraPosition.x -= movement;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            cameraPosition.x += movement;
        }
    }

    public boolean isFOVChanged() {
        return FOVChanged;
    }

    public float getZoom() {
        FOVChanged = false;
        return zoom;
    }

    public Vector2 getCameraPosition() {
        return cameraPosition;
    }
}
