package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.Game;

public class GameScreen extends ScreenAdapter {
    private boolean active;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private ScreenInputProcessor inputProcessor;
    private InputMultiplexer inputMux;
    private Game game;

    public GameScreen() {
    }

    @Override
    public void show() {
        active = true;

        batch = new SpriteBatch();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera = new OrthographicCamera();
        Debug.setCamera(camera);
        camera.position.set(5, 5, 0);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        game = new Game(batch, camera);

        // setup input processor
        inputProcessor = new ScreenInputProcessor();
        inputMux = new InputMultiplexer();
        inputMux.addProcessor(inputProcessor);
        inputMux.addProcessor(Debug.getStage());
        Gdx.input.setInputProcessor(inputMux);

    }

    private void update(float delta) {
        inputProcessor.update(delta);

        if (inputProcessor.isFOVChanged()) {
            camera.zoom = inputProcessor.getZoom();
        }
        camera.position.add(inputProcessor.getCameraMovement());
        camera.update();
        game.update(delta);
    }

    @Override
    public void render(float delta) {
        update(delta);
        batch.setProjectionMatrix(camera.combined);
        game.render();
    }

    @Override
    public void resize(int width, int height) {
        if (!active) return;
        camera.viewportWidth = Constants.VIEWPORT_SIZE;
        camera.viewportHeight = Constants.VIEWPORT_SIZE * height / width;
        camera.update();
    }

    @Override
    public void dispose() {
        if (!active) return;
        game.dispose();
        batch.dispose();
        active = false;
    }
}
