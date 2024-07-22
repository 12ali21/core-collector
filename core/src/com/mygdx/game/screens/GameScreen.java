package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Debug;
import com.mygdx.game.world.Game;

public class GameScreen extends ScreenAdapter {
    private final AssetManager assets;
    float VIEWPORT_SIZE = 50;
    private boolean active;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private ScreenInputProcessor inputProcessor;
    private Game game;

    public GameScreen(AssetManager assets) {
        this.assets = assets;
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

        game = new Game(assets, batch, camera);

        // setup input processor
        inputProcessor = new ScreenInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

    }

    private void update(float delta) {
        inputProcessor.update(delta);
        game.update(delta);

        if (inputProcessor.isFOVChanged())
            camera.zoom = inputProcessor.getZoom();
        camera.position.set(inputProcessor.getCameraPosition(), 0);
        camera.update();
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
        camera.viewportWidth = VIEWPORT_SIZE;
        camera.viewportHeight = VIEWPORT_SIZE * height / width;
        camera.update();
    }

    @Override
    public void dispose() {
        if (!active) return;
        batch.dispose();
        active = false;
    }
}
