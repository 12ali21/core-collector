package com.mygdx.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Renderable;
import com.mygdx.game.Updatable;
import com.mygdx.game.audio.AudioManager;
import com.mygdx.game.audio.BackgroundMusic;
import com.mygdx.game.entities.EntityManager;
import com.mygdx.game.entities.bots.BotManager;
import com.mygdx.game.entities.enemies.EnemiesManager;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.others.Bullet;
import com.mygdx.game.entities.others.HoveredTile;
import com.mygdx.game.entities.structures.Ship;
import com.mygdx.game.entities.structures.StructureBuilder;
import com.mygdx.game.entities.structures.Structures;
import com.mygdx.game.screens.ScreenInputProcessor;
import com.mygdx.game.ui.UIManager;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.world.map.MapManager;

public class Game implements Renderable, Updatable, Disposable {
    public final MapManager map;
    public final AudioManager audio;
    public final UIManager ui;
    public final EntityManager entities;
    public final StructureBuilder builder = new StructureBuilder(this);
    private final EnemiesManager enemies;

    private final World world;
    private final Batch batch;
    private final OrthographicCamera camera;

    private final Box2DDebugRenderer debugRenderer;
    private final HoveredTile hoveredTile;
    private final MyContactListener contactListener;
    private final BackgroundMusic backgroundMusic;
    private final ScreenInputProcessor inputProcessor;
    private final Ship ship;
    private final BotManager bots;

    Array<Scheduler> schedulers = new Array<>();
    private float timeStep = 1 / 60f;


    public Game(Batch batch, OrthographicCamera camera) {
        this.world = new World(new Vector2(0, 0), true);
        this.debugRenderer = new Box2DDebugRenderer();
        this.batch = batch;
        this.camera = camera;
        entities = new EntityManager(this);

        audio = new AudioManager(camera);
        ui = new UIManager(this);


        // setup input processors
        inputProcessor = new ScreenInputProcessor();
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(inputProcessor);
        inputMux.addProcessor(Debug.getStage());
        inputMux.addProcessor(ui.getProcessor());
        inputMux.addProcessor(entities);
        Gdx.input.setInputProcessor(inputMux);

        contactListener = new MyContactListener();
        setContactListeners();

        map = new MapManager(this, "maze");
        GridPoint2 mapCenter = new GridPoint2((int) (map.getWidth() / 2f), (int) (map.getHeight() / 2f));
        map.emptySpace(mapCenter.x, mapCenter.y, 10, 10);

        camera.position.set(map.getWidth() / 2f, map.getHeight() / 2f, 0);
        camera.update();

        hoveredTile = new HoveredTile(this);

        enemies = new EnemiesManager(this);

        ship = (Ship) Structures.ship(this, mapCenter.x, mapCenter.y).build();
        entities.addStructure(ship);

        backgroundMusic = new BackgroundMusic(this);

        bots = new BotManager(this, 8);

        updatePreferences();
    }

    private void setContactListeners() {
        world.setContactListener(contactListener);
        contactListener.registerCallback(Bullet::handleContact);
    }

    @Override
    public void update(float delta) {
        // increase game speed (for debugging)
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            delta = delta * 6;
            timeStep = 1 / 10f;
        } else {
            timeStep = 1 / 60f;
        }

        ui.update(delta);

        // handle pause
        if (handlePause(delta)) return;

        world.step(timeStep, 6, 2);

        updateManagers(delta);
        tickSchedulers(delta);
        entities.update(delta);
    }

    private void updateManagers(float delta) {
        builder.update(delta);
        enemies.update(delta);
        MessageManager.getInstance().update();
        audio.update(delta);
        backgroundMusic.update(delta);
        bots.update(delta);
    }

    private void tickSchedulers(float delta) {
        for (Scheduler s : schedulers) {
            s.update(delta);
        }
    }

    private boolean handlePause(float delta) {
        if (!ui.isMenuPaused()) {
            updateCameraMovement(delta);

            Vector3 mousePos = unproject(Gdx.input.getX(), Gdx.input.getY());
            hoveredTile.findPosition(mousePos);
            hoveredTile.update(delta);
        }

        if (ui.isPaused()) {
            backgroundMusic.pause();
        } else {
            backgroundMusic.resume();
        }
        return ui.isPaused();
    }

    private void updateCameraMovement(float delta) {
        inputProcessor.update(delta);

        if (inputProcessor.isFOVChanged()) {
            camera.zoom = inputProcessor.getZoom();
        }
        camera.position.add(inputProcessor.getCameraMovement());
        camera.update();
    }

    public Vector3 unproject(float x, float y) {
        return camera.unproject(new Vector3(x, y, 0));
    }

    @Override
    public void render() {
        map.render();
        batch.begin();

        builder.render();
        hoveredTile.render();
        entities.render();
        batch.end();
        if (Debug.isDebugging()) {
            debugRenderer.render(world, camera.combined);
        }
        ui.render();
    }

    /**
     * check the structures on map to see if they are occupied
     *
     * @return true if the tiles are occupied
     */
    public boolean areTilesOccupied(int x, int y, int width, int height) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (map.isTileOccupied(i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Registers a scheduler to be updated automatically
     */
    public Scheduler registerScheduler(Scheduler scheduler) {
        schedulers.add(scheduler);
        return scheduler;
    }


    // ---------------- Getters ----------------
    public World getWorld() {
        return world;
    }

    public Batch getBatch() {
        return batch;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Array<Enemy> getEnemies() {
        return enemies.getEnemies();
    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        map.dispose();
        audio.dispose();
    }

    public void updatePreferences() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        audio.setMusicVolume(prefs.getFloat(Constants.PREFS_BGM_VOLUME, 0.2f));
        audio.setSoundEffectsVolume(prefs.getFloat(Constants.PREFS_SFX_VOLUME, 0.1f));
    }

    public Ship getShip() {
        return ship;
    }
}
