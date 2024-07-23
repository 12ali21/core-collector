package com.mygdx.game.world;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;
import box2dLight.RayHandlerOptions;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Drawable;
import com.mygdx.game.Updatable;
import com.mygdx.game.entities.Bullet;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.HoveredTile;
import com.mygdx.game.entities.enemies.Enemy;
import com.mygdx.game.entities.enemies.RedCreep;
import com.mygdx.game.entities.structures.Bounds;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.world.map.Map;

import java.util.Iterator;

public class Game implements Drawable, Updatable {
    public final Map map;

    private final World world;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final AssetManager assets;
    private final Box2DDebugRenderer debugRenderer;
    private final StructureBuilder structureBuilder = new StructureBuilder(this);
    private final HoveredTile hoveredTile;
    private final Array<Entity> entitiesRender = new Array<>();
    private final Array<Entity> entitiesUpdate = new Array<>();
    private final Array<Entity> entitiesToAdd = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private final MyContactListener contactListener;
    RayHandler rayHandler;
    private boolean isSorted = false;

    public Game(AssetManager assets, Batch batch, OrthographicCamera camera) {
        this.world = new World(new Vector2(0, 0), true);
        RayHandlerOptions options = new RayHandlerOptions();
//        options.setGammaCorrection(true);
        options.setDiffuse(true);
        options.setPseudo3d(true);

        rayHandler = new RayHandler(world, options);

        rayHandler.setAmbientLight(1f);
        DirectionalLight sunlight = new DirectionalLight(rayHandler, 100, Color.WHITE, -90);
//        sunlight.setSoftnessLength(5f);
//        sunlight.setSoft(true);
        sunlight.setHeight(10);

//        sunlight.setStaticLight(true);
//        pointLight = new PointLight(rayHandler, 100, Color.WHITE, 1, 0, 0);

        contactListener = new MyContactListener();
        setContactListeners();

        this.debugRenderer = new Box2DDebugRenderer();
        this.assets = assets;
        this.batch = batch;
        this.camera = camera;
        map = new Map(this, "maze");

        hoveredTile = new HoveredTile(this);
        addEntity(hoveredTile);
        Enemy creep = new RedCreep(this, new Vector2(1.5f, 1.5f));
        addEntity(creep);

        enemies.add(creep);
    }

    private void setContactListeners() {
        world.setContactListener(contactListener);
        contactListener.registerCallback(Bullet::handleContact);
    }

    @Override
    public void update(float delta) {
        world.step(1 / 60f, 6, 2);
        rayHandler.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth, camera.viewportHeight);
        rayHandler.update();
        Vector3 mousePos = unproject(Gdx.input.getX(), Gdx.input.getY());
        hoveredTile.findPosition(mousePos);

        structureBuilder.update(delta);

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
        debugRenderer.render(world, camera.combined);
        rayHandler.render();
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

    public AssetManager getAssets() {
        return assets;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }
}
