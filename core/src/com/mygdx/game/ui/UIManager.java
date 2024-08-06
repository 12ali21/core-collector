package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Renderable;
import com.mygdx.game.Updatable;
import com.mygdx.game.audio.NonSpatialSound;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.entities.structures.Structures;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class UIManager implements Renderable, Updatable, Disposable {
    private final Stage stage;
    private final Table uiTable;
    private TextButton shipButton;
    private final Table pauseMenu;
    private final Label pausedLabel;
    private final Game game;
    private final Table optionsMenu;
    private final Table builderTable;
    private TextureRegionDrawable pauseBackgroundDrawable;

    private boolean menuPaused;
    private boolean paused;
    private final NonSpatialSound pauseSound;

    private boolean building = false;

    public UIManager(Game game) {
        this.game = game;
        Skin skin = Constants.SKIN;
        pauseSound = game.audio.newNonSpatialSoundEffect(AudioAssets.PAUSE_SOUND, .5f);


        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(false);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    if (!menuPaused) {
                        paused = !paused;
                        pauseSound.play();
                        return true;
                    }
                }
                if (keycode == Input.Keys.ESCAPE) {
                    handleEscape();
                    return true;
                }
                return super.keyDown(event, keycode);
            }
        });
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (building && !builderTable.isVisible()) {
                    showBuilder();
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.pad(20f);
        stage.addActor(root);

        pausedLabel = new Label("PAUSED", skin);
        pausedLabel.setVisible(false);
        root.add(pausedLabel).center().colspan(2).row();

        // Main UI
        uiTable = new Table();
        root.addActor(uiTable);
        uiTable.setFillParent(true);
        fillUITable(uiTable, skin);

        // Structure Builder Menu UI
        builderTable = new Table(skin);
        buildStructureBuilderTable(root, skin);

        // Pause Menu UI
        pauseMenu = buildPauseMenuTable(skin);
        optionsMenu = buildOptionsMenuTable(skin);
    }

    private void fillUITable(Table root, Skin skin) {
        Button buildButton = new Button(skin);
        root.add(buildButton)
                .expand()
                .bottom()
                .left();

        Image image = new Image(TextureAssets.get(TextureAssets.BUILDING_ICON));
        buildButton.add(image).maxSize(100f, 100f).expand().pad(4f);
        buildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showBuilder();
            }
        });


        // Ship Button
        shipButton = new TextButton(Constants.START, skin);
        root.add(shipButton)
                .size(250f, 100f)
                .expand()
                .bottom()
                .right();
        shipButton.getLabel().setFontScale(1.2f);
    }

    private void buildStructureBuilderTable(Table root, Skin skin) {
        builderTable.setBackground("progress-bar-back");
        builderTable.setVisible(false);
        root.add(builderTable).expand().growX().bottom();

//        button.setDebug(true, true);
        builderTable.add(getStructureButton(skin, Structures.basicTurret(game, 0, 0),
                () -> game.builder.setBuild(Structures.basicTurret(game, 0, 0))));
        builderTable.add(getStructureButton(skin, Structures.burstTurret(game, 0, 0),
                () -> game.builder.setBuild(Structures.burstTurret(game, 0, 0))));

    }

    private Button getStructureButton(Skin skin, Structure.Builder build, Runnable runnable) {
        Texture basicTexture = build.getIconTexture();
        Image image = new Image(basicTexture);
        image.setOrigin(Align.center);
        image.setRotation(90);
        image.setSize(128f, 128f);

        Button button = new Button(skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideBuilder();
                runnable.run();
            }
        });
        button.add(image).pad(32f, 16f, 32f, 16f);
        return button;
    }

    private Table buildPauseMenuTable(Skin skin) {
        final Table pauseMenu;
        pauseMenu = new Table(skin);
        pauseMenu.setFillParent(true);
        pauseMenu.setVisible(false);
        pauseMenu.setBackground(getPauseMenuBackground());
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
                menuPaused = false;
                paused = false;
                pauseSound.play();
                setPauseMenuVisible(false);
            }
        });
        pauseMenu.add(resumeButton).row();

        TextButton optionsButton = new TextButton("Options", skin);
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showOptions();
            }
        });
        pauseMenu.add(optionsButton).row();

        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        pauseMenu.add(exitButton).row();
        pauseMenu.defaults().reset();
        return pauseMenu;
    }

    private void showOptions() {
        optionsMenu.setVisible(true);
        //TODO: reset slider positions
    }

    private Table buildOptionsMenuTable(Skin skin) {
        final Table optionsRoot = new Table(skin);
        optionsRoot.setFillParent(true);
        optionsRoot.setVisible(false);
//        optionsRoot.setBackground(getBackground());
        stage.addActor(optionsRoot);

        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);

        Window window = new Window("Options", skin);
        window.getTitleLabel().setAlignment(Align.center);
        optionsRoot.add(window).grow().pad(64).maxSize(1000, 800);

        window.defaults().pad(16, 16, 0, 16).top().expandX().fillX().uniformX();
        Table bgmOptions = sliderOptionsTable(skin, "Background Music", prefs.getFloat(Constants.PREFS_BGM_VOLUME, 0.5f));
        Slider bgmSlider = (Slider) bgmOptions.getChild(1);

        Table sfxOptions = sliderOptionsTable(skin, "SFX", prefs.getFloat(Constants.PREFS_SFX_VOLUME, 0.5f));
        Slider sfxSlider = (Slider) sfxOptions.getChild(1);

        window.add(bgmOptions).row();
        window.add(sfxOptions).row();
        window.add(makeOptionsButtonsTable(skin, () -> {
            prefs.putFloat(Constants.PREFS_BGM_VOLUME, bgmSlider.getValue());
            prefs.putFloat(Constants.PREFS_SFX_VOLUME, sfxSlider.getValue());
            prefs.flush();
            game.updatePreferences();
        })).growX().expand().bottom();
        return optionsRoot;
    }

    private Table sliderOptionsTable(Skin skin, String title, float value) {
        Table group = new Table(skin);

        Label titleLabel = new Label(title, skin);

        Slider slider = new Slider(0f, 1f, 0.05f, false, skin);
        slider.setValue(value);
        Label label = new Label(String.format("%.2f", slider.getValue()), skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                label.setText(String.format("%.2f", slider.getValue()));
            }
        });

        group.defaults().pad(12).expandX().top();
        group.add(titleLabel).width(250f).left();
        group.add(slider).minWidth(300f).right().top();
        group.add(label).minWidth(100f).right().row();
        group.defaults().reset();
        return group;
    }

    private Table makeOptionsButtonsTable(Skin skin, Runnable onAccept) {
        Table buttons = new Table();

        buttons.defaults().minWidth(200f).minHeight(75f).uniform();

        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                optionsMenu.setVisible(false);
            }
        });
        buttons.add(cancelButton).expand().left();

        TextButton acceptButton = new TextButton("Accept", skin);
        acceptButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onAccept.run();
                optionsMenu.setVisible(false);
            }
        });
        buttons.add(acceptButton).expand().right();
        buttons.defaults().reset();
        return buttons;
    }

    private void showBuilder() {
        builderTable.setVisible(true);
        uiTable.setVisible(false);
        building = true;
    }

    private void hideBuilder() {
        builderTable.setVisible(false);
    }

    private void closeBuilder() {
        hideBuilder();
        uiTable.setVisible(true);
        building = false;
    }

    private void handleEscape() {
        if (building) {
            closeBuilder();
            return;
        }

        if (!menuPaused) {
            menuPaused = true;
            paused = true;
            setPauseMenuVisible(true);
        } else {
            menuPaused = false;
            paused = false;
            setPauseMenuVisible(false);
        }
        pauseSound.play();
    }

    private TextureRegionDrawable getPauseMenuBackground() {
        if (pauseBackgroundDrawable != null) {
            return pauseBackgroundDrawable;
        }
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.4f);
        pixmap.fill();
        pauseBackgroundDrawable = new TextureRegionDrawable(new Texture(pixmap));
        return pauseBackgroundDrawable;
    }

    public void setPauseMenuVisible(boolean visible) {
        pauseMenu.setVisible(visible);
        if (!visible && optionsMenu.isVisible())
            optionsMenu.setVisible(false);
    }

    public void setPauseLabelVisible(boolean visible) {
        pausedLabel.setVisible(visible);
    }

    public void setShipButtonListener(EventListener listener) {
        shipButton.addListener(listener);
    }

    @Override
    public void render() {
        stage.draw();
    }

    @Override
    public void update(float delta) {
        stage.act(delta);
        setPauseLabelVisible(paused);
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

    public boolean isPaused() {
        return paused;
    }

    public boolean isMenuPaused() {
        return menuPaused;
    }
}
