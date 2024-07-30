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
    private Screen gameScreen;
    private Screen mainMenuScreen;
    private Screen currentScreen;
    private AssetManager assets;
    private boolean loaded;

    @Override
    public void create() {
        assets = new AssetManager();
        TextureAssets.loadAll(assets);
        AudioAssets.loadAll(assets);

        mainMenuScreen = new MainMenuScreen(() -> System.out.println("New game"), () -> {
            gameScreen = new GameScreen();
            gameScreen.show();
            currentScreen = gameScreen;
        });
        currentScreen = gameScreen = new GameScreen(); // FIXME: for convenience
//        gameScreen = new GameScreen();
//		gameScreen.show();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        if (assets.update()) {
            if (!loaded) {
                currentScreen.show();
            }
            loaded = true;
            currentScreen.render(Gdx.graphics.getDeltaTime());
            Debug.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void resize(int width, int height) {
        currentScreen.resize(width, height);
        Debug.resize(width, height);
    }

    @Override
    public void dispose() {
        currentScreen.dispose();
        TextureAssets.unloadAll(assets);
        AudioAssets.unloadAll(assets);
        assets.dispose();
        Debug.dispose();
    }

}
