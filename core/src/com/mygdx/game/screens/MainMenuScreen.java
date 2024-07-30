package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen extends ScreenAdapter {

    private final Runnable newGame;
    private final Runnable continueGame;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Runnable newGame, Runnable continueGame) {
        this.newGame = newGame;
        this.continueGame = continueGame;
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("ui/sgx-ui.json"));

        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(false);
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(200f);

        stage.addActor(root);

        root.defaults()
                .space(10f)
                .uniform()
                .fill()
                .maxWidth(300f)
                .height(40f)
                .growX();
        TextButton newGameButton = new TextButton("New Game", skin);
        root.add(newGameButton);
        root.row();

        TextButton continueButton = new TextButton("Continue", skin);
        root.add(continueButton);
        root.row();

        TextButton settingsButton = new TextButton("Settings", skin);
        root.add(settingsButton);
        root.row();

        TextButton exitButton = new TextButton("Exit", skin);
        root.add(exitButton);
        root.row();

        root.defaults().reset();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                newGame.run();
            }
        });

        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                continueGame.run();
            }
        });
    }


    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }
}
