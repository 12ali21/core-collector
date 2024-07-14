package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen extends ScreenAdapter {
    private boolean active;
    private SpriteBatch batch;

    private OrthographicCamera camera;
    private ScreenInputProcessor inputProcessor;
    private World world;

    float VIEWPORT_SIZE = 50;

    private final AssetManager assets;
    public GameScreen(AssetManager assets) {
        this.assets = assets;
    }

    @Override
    public void show() {
        active = true;

        batch = new SpriteBatch();

        camera = new OrthographicCamera(VIEWPORT_SIZE, VIEWPORT_SIZE);
        camera.position.set(5, 5, 0);
        camera.update();
        world = new World(assets, batch, camera);

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
        if (!active) return;
        camera.viewportWidth = VIEWPORT_SIZE;
        camera.viewportHeight = VIEWPORT_SIZE * height/width;
        camera.update();
    }

    @Override
    public void dispose() {
        if (!active) return;
        batch.dispose();
        active = false;
    }
}