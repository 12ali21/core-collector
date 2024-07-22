package com.mygdx.game.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.Debug;

public class CoreGame extends ApplicationAdapter {
	Screen gameScreen;
	private AssetManager assets;
	private boolean loaded;
	@Override
	public void create () {
		assets = new AssetManager();
		assets.load("sprites/bot.png", Texture.class);
		assets.load("sprites/bullet.png", Texture.class);
		assets.load("sprites/enemy_big.png", Texture.class);
		assets.load("sprites/enemy_small.png", Texture.class);
		assets.load("sprites/hovered_tile.png", Texture.class);
		assets.load("sprites/ship.png", Texture.class);
		assets.load("sprites/turret_base.png", Texture.class);
		assets.load("sprites/turret_head.png", Texture.class);
		assets.load("maps/ground/1.png", Texture.class);

		gameScreen = new GameScreen(assets);
//		gameScreen.show();
	}

	@Override
	public void render () {
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
	public void dispose () {
		gameScreen.dispose();
		Debug.dispose();
	}

}
