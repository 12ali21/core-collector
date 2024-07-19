package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ScreenInputProcessor extends InputAdapter implements Updatable {

    public static final float ZOOM_LIMIT_LOW = 0.25f;
    public static final float ZOOM_LIMIT_HIGH = 4f;
    public static final float ZOOM_CHANGE_SPEED =0.1f;

    public static final float MOVE_SPEED = 7f;

    private float zoom = 1f;
    private final Vector2 cameraPosition = new Vector2(0, 0);

    private boolean FOVChanged = false;

    @Override
    public boolean scrolled(float amountX, float amountY) {
        zoom += amountY * ZOOM_CHANGE_SPEED;
        zoom = MathUtils.clamp(zoom, ZOOM_LIMIT_LOW, ZOOM_LIMIT_HIGH);
        FOVChanged = true;
        return true;
    }

    @Override
    public void update(float delta) {
        float movement = MOVE_SPEED * delta * 1f;
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
