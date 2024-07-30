package com.mygdx.game.ui;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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

    public UIManager() {
        Skin skin = Constants.SKIN;

        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(false);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(20f);
        stage.addActor(root);

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
}
