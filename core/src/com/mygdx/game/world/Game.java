package com.mygdx.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
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
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.audio.AudioManager;
import com.mygdx.game.audio.BackgroundMusic;
import com.mygdx.game.audio.NonSpatialSound;
import com.mygdx.game.entities.enemies.EnemiesManager;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.others.Bullet;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.others.HoveredTile;
import com.mygdx.game.entities.structures.Bounds;
import com.mygdx.game.entities.structures.Ship;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.entities.structures.Structures;
import com.mygdx.game.screens.ScreenInputProcessor;
import com.mygdx.game.ui.UIManager;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.map.MapManager;

import java.util.Iterator;

public class Game implements Renderable, Updatable, Disposable, Telegraph {
    public final MapManager map;
    public final AudioManager audio;
    public final UIManager ui;

    private final World world;
    private final Batch batch;
    private final OrthographicCamera camera;

    private final Box2DDebugRenderer debugRenderer;
    private final StructureBuilder structureBuilder = new StructureBuilder(this);
    private final HoveredTile hoveredTile;
    private final Array<Entity> entitiesRender = new Array<>();
    private final Array<Entity> entitiesUpdate = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private final MyContactListener contactListener;
    private final NonSpatialSound pauseSound;
    private final EnemiesManager enemiesManager;
    private final BackgroundMusic backgroundMusic;
    private final ScreenInputProcessor inputProcessor;
    private boolean isSorted = false;
    private boolean isPaused = false;
    private boolean isMenuPaused = false;


    public Game(Batch batch, OrthographicCamera camera) {
        this.world = new World(new Vector2(0, 0), true);
        this.debugRenderer = new Box2DDebugRenderer();
        this.batch = batch;
        this.camera = camera;

        ui = new UIManager(this, new UIManager.PauseMenuListener() {
            @Override
            public void onResume() {
                isMenuPaused = false;
                isPaused = false;
                pauseSound.play();
            }

            @Override
            public void onExit() {
                Gdx.app.exit();
            }
        });

        // setup input processors
        inputProcessor = new ScreenInputProcessor();
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(inputProcessor);
        inputMux.addProcessor(Debug.getStage());
        inputMux.addProcessor(ui.getProcessor());
        Gdx.input.setInputProcessor(inputMux);

        contactListener = new MyContactListener();
        setContactListeners();

        map = new MapManager(this, "maze");
        GridPoint2 mapCenter = new GridPoint2((int) (map.getWidth() / 2f), (int) (map.getHeight() / 2f));
        map.emptySpace(mapCenter.x, mapCenter.y, 10, 10);
        audio = new AudioManager(camera);
        pauseSound = audio.newNonSpatialSoundEffect(AudioAssets.PAUSE_SOUND, .5f);

        camera.position.set(map.getWidth() / 2f, map.getHeight() / 2f, 0);
        camera.update();

        hoveredTile = new HoveredTile(this);

        enemiesManager = new EnemiesManager(this);

        Ship ship = (Ship) Structures.ship(this, mapCenter.x, mapCenter.y).build();
        addStructure(ship);

        backgroundMusic = new BackgroundMusic(this);
        MessageManager.getInstance().addListener(this, MessageType.SHIP_STARTED.ordinal());

        updatePreferences();
    }

    private void setContactListeners() {
        world.setContactListener(contactListener);
        contactListener.registerCallback(Bullet::handleContact);
    }

    @Override
    public void update(float delta) {

        // handle pause
        if (handlePause(delta)) return;

        world.step(1 / 60f, 6, 2);

        updateManagers(delta);

        // Update entities
        for (Iterator<Entity> itr = entitiesUpdate.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.update(delta);
            } else {
                if (entity instanceof Structure) { // since structure need to be removed from map
                    Structure s = (Structure) entity;
                    Bounds bounds = s.getBounds();
                    map.removeStructure(s, bounds.x, bounds.y, bounds.width, bounds.height);
                }
                entity.dispose();
                itr.remove();
            }
        }

        registerEntities();
    }

    private void updateManagers(float delta) {
        structureBuilder.update(delta);
        enemiesManager.update(delta);
        MessageManager.getInstance().update();
        audio.update(delta);
        backgroundMusic.update(delta);
        ui.update(delta);
    }

    private boolean handlePause(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!isMenuPaused) {
                isPaused = !isPaused;
                pauseSound.play();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!isMenuPaused) {
                isMenuPaused = true;
                isPaused = true;
                ui.setPauseMenuVisible(true);
            } else {
                isMenuPaused = false;
                isPaused = false;
                ui.setPauseMenuVisible(false);
            }
            pauseSound.play();

        }

        if (!isMenuPaused) {
            updateCameraMovement(delta);

            Vector3 mousePos = unproject(Gdx.input.getX(), Gdx.input.getY());
            hoveredTile.findPosition(mousePos);
            hoveredTile.update(delta);
        }

        if (isPaused) {
            backgroundMusic.pause();
        } else {
            backgroundMusic.resume();
        }
        ui.setPauseLabelVisible(isPaused);
        return isPaused;
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

        structureBuilder.render();
        hoveredTile.render();
        // Sort entities by rendering order
        if (!isSorted) {
            entitiesRender.sort((a, b) -> Float.compare(a.getRenderPriority(), b.getRenderPriority()));
            isSorted = true;
        }

        // Render entities
        for (Iterator<Entity> itr = entitiesRender.iterator(); itr.hasNext(); ) {
            Entity entity = itr.next();
            if (entity.isAlive()) {
                entity.render();
            } else {
                itr.remove();
            }
        }
        batch.end();
        if (Debug.isDebugging()) {
            debugRenderer.render(world, camera.combined);
        }
        ui.render();
    }

    /**
     * Add an entity to the world
     */
    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Add a structure to the world and map
     */
    public void addStructure(Structure structure) {
        Bounds bounds = structure.getBounds();
        map.putNewStructure(structure, bounds.x, bounds.y, bounds.width, bounds.height);

        entitiesToAdd.add(structure);
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
     * Register new entities this frame to be updated and rendered
     */
    private void registerEntities() {
        if (entitiesToAdd.isEmpty()) {
            return;
        }
        for (Entity e : entitiesToAdd) {
            // If the entity is a structure, add its parts instead
            if (e instanceof Structure) {
                Structure s = (Structure) e;
                for (Entity e2 : s.getParts()) {
                    entitiesRender.add(e2);
                }
            }
            entitiesRender.add(e);
            entitiesUpdate.add(e);
        }
        isSorted = false;
        entitiesToAdd.clear();
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
        return enemiesManager.getEnemies();
    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        map.dispose();
        audio.dispose();
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }

    public void updatePreferences() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        audio.setMusicVolume(prefs.getFloat(Constants.PREFS_BGM_VOLUME, 0.2f));
        audio.setSoundEffectsVolume(prefs.getFloat(Constants.PREFS_SFX_VOLUME, 0.1f));
    }
}
