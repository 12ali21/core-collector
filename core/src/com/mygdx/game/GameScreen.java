package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.entities.Drawable;
import com.mygdx.game.entities.Updatable;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {
    private final SpriteBatch batch;

    private final OrthographicCamera camera;
    private final ScreenInputProcessor inputProcessor;
    private final World world;

    float VIEWPORT_SIZE = 20;

    public GameScreen() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera(VIEWPORT_SIZE, VIEWPORT_SIZE);
        camera.position.set(5, 5, 0);
        camera.update();
        world = new World(batch, camera);

        // setup input processor
        inputProcessor = new ScreenInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

    }

    private void update(float delta) {
        inputProcessor.update(delta);
        world.update(delta);

        camera.zoom = inputProcessor.getZoom();
        camera.position.set(inputProcessor.getCameraPosition(), 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        update(delta);
        batch.setProjectionMatrix(camera.combined);
        world.render();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = VIEWPORT_SIZE;
        camera.viewportHeight = VIEWPORT_SIZE * height/width;
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
