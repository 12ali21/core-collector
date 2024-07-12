package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class CoreGame extends ApplicationAdapter {
	Screen gameScreen;
	
	@Override
	public void create () {
		gameScreen = new GameScreen();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		gameScreen.render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		gameScreen.resize(width, height);
	}

	@Override
	public void dispose () {
		gameScreen.dispose();
	}

}
