package com.mygdx.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.audio.AudioManager;
import com.mygdx.game.audio.NonSpatialSound;
import com.mygdx.game.entities.enemies.EnemiesManager;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.enemies.RedCreep;
import com.mygdx.game.entities.others.Bullet;
import com.mygdx.game.entities.others.Entity;
import com.mygdx.game.entities.others.HoveredTile;
import com.mygdx.game.entities.structures.Bounds;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.world.map.MapManager;

import java.util.Iterator;

public class Game implements Drawable, Updatable, Disposable {
    public final MapManager map;
    public final AudioManager audio;

    private final World world;
    private final Batch batch;
    private final OrthographicCamera camera;

    private final Box2DDebugRenderer debugRenderer;
    private final StructureBuilder structureBuilder = new StructureBuilder(this);
    private final HoveredTile hoveredTile;
    private final Array<Entity> entitiesRender = new Array<>();
    private final Array<Entity> entitiesUpdate = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private final MyContactListener contactListener;
    private final NonSpatialSound pauseSound;
    private final Music ambientMusic;
    private final EnemiesManager enemiesManager;
    private boolean isSorted = false;
    private boolean isPaused = false;

    public Game(Batch batch, OrthographicCamera camera) {
        this.world = new World(new Vector2(0, 0), true);
        contactListener = new MyContactListener();
        setContactListeners();

        this.debugRenderer = new Box2DDebugRenderer();
        this.batch = batch;
        this.camera = camera;
        map = new MapManager(this, "maze");
        audio = new AudioManager(camera);

        camera.position.set(map.getWidth() / 2f, map.getHeight() / 2f, 0);
        camera.update();

        hoveredTile = new HoveredTile(this);
        addEntity(hoveredTile);
        pauseSound = audio.newNonSpatialSoundEffect(AudioAssets.PAUSE_SOUND);
        audio.setSoundEffectsVolume(0.1f);

        ambientMusic = audio.newMusic(AudioAssets.AMBIENT_MUSIC);
        ambientMusic.setLooping(true);
        ambientMusic.setVolume(0.4f);
        ambientMusic.play();

        Debug.addButton("Spawn Enemy", () -> {
            Enemy enemy = new RedCreep(this, new Vector2(1.5f, 1.5f));
            addEntity(enemy);
            enemies.add(enemy);
        });

        enemiesManager = new EnemiesManager();
    }

    private void setContactListeners() {
        world.setContactListener(contactListener);
        contactListener.registerCallback(Bullet::handleContact);
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            isPaused = !isPaused;
            pauseSound.play();
        }
        if (isPaused) {
            return;
        }

        world.step(1 / 60f, 6, 2);

        Vector3 mousePos = unproject(Gdx.input.getX(), Gdx.input.getY());
        hoveredTile.findPosition(mousePos);

        structureBuilder.update(delta);
        enemiesManager.update(delta);
        MessageManager.getInstance().update();
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
                } else if (entity instanceof Enemy) {
                    enemies.removeValue((Enemy) entity, true);
                }
                entity.dispose();
                itr.remove();
            }
        }

        registerEntities();
        audio.update(delta);
    }

    public Vector3 unproject(float x, float y) {
        return camera.unproject(new Vector3(x, y, 0));
    }

    @Override
    public void render() {
        map.render();        // Map renderer has its own batch, so render it first
        batch.begin();

        structureBuilder.render();
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
        return enemies;
    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        map.dispose();
        ambientMusic.dispose();
        audio.dispose();
    }
}
