package com.mygdx.game.ui;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.TextureAssets;

public class UIManager implements Drawable, Updatable, Disposable {
    private final Stage stage;
    private final TextButton shipButton;
    private final Button buildButton;
    private final Table pauseMenu;
    private final Label pausedLabel;

    public UIManager(PauseMenuListener menuListener) {
        Skin skin = Constants.SKIN;

        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(false);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(20f);
        stage.addActor(root);

        pausedLabel = new Label("PAUSED", skin);
        pausedLabel.setVisible(false);
        root.add(pausedLabel).center().colspan(2).row();

        // Structure Builder Button
        buildButton = new Button(skin);
        root.add(buildButton)
                .expand()
                .bottom()
                .left();

        Image image = new Image(TextureAssets.get(TextureAssets.BUILDING_ICON));
        buildButton.add(image).expand().pad(4f);


        // Ship Button
        shipButton = new TextButton(Constants.START, skin);
        root.add(shipButton)
                .size(250f, 100f)
                .expand()
                .bottom()
                .right();
        shipButton.getLabel().setFontScale(1.2f);

        // Pause Menu

        pauseMenu = new Table();
        pauseMenu.setFillParent(true);
        pauseMenu.setVisible(false);
        pauseMenu.setBackground(getBackground());
        stage.addActor(pauseMenu);

        pauseMenu.defaults()
                .space(4f)
                .uniform()
                .maxWidth(250f)
                .height(70f)
                .growX();

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                menuListener.onResume();
                setPauseMenuVisible(false);
            }
        });
        pauseMenu.add(resumeButton).row();

        TextButton optionsButton = new TextButton("Options", skin);
        pauseMenu.add(optionsButton).row();

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                menuListener.onExit();
            }
        });
        pauseMenu.add(exitButton).row();
        pauseMenu.defaults().reset();


    }

    private TextureRegionDrawable getBackground() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.4f);
        pixmap.fill();
        return new TextureRegionDrawable(new Texture(pixmap));
    }

    public void setPauseMenuVisible(boolean visible) {
        pauseMenu.setVisible(visible);
    }

    public void setPauseLabelVisible(boolean visible) {
        pausedLabel.setVisible(visible);
    }

    public void setShipButtonListener(EventListener listener) {
        shipButton.addListener(listener);
    }

    public void setBuildButtonListener(EventListener listener) {
        buildButton.addListener(listener);
    }

    @Override
    public void render() {
        stage.draw();
    }

    @Override
    public void update(float delta) {
        stage.act(delta);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
    }

    public InputProcessor getProcessor() {
        return stage;
    }

    public interface PauseMenuListener {
        void onResume();

        void onExit();
    }
}
