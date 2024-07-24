package com.mygdx.game.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.TextureAssets;

public class CoreGame extends ApplicationAdapter {
    Screen gameScreen;
    private AssetManager assets;
    private boolean loaded;

    @Override
    public void create() {
        assets = new AssetManager();
        TextureAssets.loadAll(assets);
        AudioAssets.loadAll(assets);

        gameScreen = new GameScreen();
//		gameScreen.show();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        if (assets.update()) {
            if (!loaded) {
                gameScreen.show();
            }
            loaded = true;
            gameScreen.render(Gdx.graphics.getDeltaTime());
            Debug.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void resize(int width, int height) {
        gameScreen.resize(width, height);
        Debug.resize(width, height);
    }

    @Override
    public void dispose() {
        gameScreen.dispose();
        TextureAssets.unloadAll(assets);
        AudioAssets.unloadAll(assets);
        assets.dispose();
        Debug.dispose();
    }

}
