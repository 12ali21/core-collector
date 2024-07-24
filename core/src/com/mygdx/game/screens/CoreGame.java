package com.mygdx.game.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;

public class CoreGame extends ApplicationAdapter {
    Screen gameScreen;
    private AssetManager assets;
    private boolean loaded;

    @Override
    public void create() {
        assets = new AssetManager();
        assets.load(Constants.BOT_TEXTURE, Texture.class);
        assets.load(Constants.BULLET_TEXTURE, Texture.class);
        assets.load(Constants.ENEMY_BIG_TEXTURE, Texture.class);
        assets.load(Constants.ENEMY_SMALL_TEXTURE, Texture.class);
        assets.load(Constants.HOVERED_TILE_TEXTURE, Texture.class);
        assets.load(Constants.SHIP_TEXTURE, Texture.class);
        assets.load(Constants.TURRET_BASE_TEXTURE, Texture.class);
        assets.load(Constants.TURRET_HEAD_TEXTURE, Texture.class);
        assets.load(Constants.TURRET_HEAD_MULTI_TEXTURE, Texture.class);
        assets.load(Constants.GROUND_TEXTURE, Texture.class);
        assets.load(Constants.HEALTH_BORDER_TEXTURE, Texture.class);
        assets.load(Constants.GREEN_BAR_TEXTURE, Texture.class);
        assets.load(Constants.YELLOW_BAR_TEXTURE, Texture.class);
        assets.load(Constants.RED_BAR_TEXTURE, Texture.class);
        assets.load(Constants.DIRT_WALL_TEXTURE, Texture.class);

        gameScreen = new GameScreen(assets);
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
        Debug.dispose();
    }

}
