package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Updatable;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.ai.formation.FormationManager;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Debug;
import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.world.Game;

public class EnemiesManager implements Updatable, Telegraph {
    private static final float MEAN = 10;
    private static final float DEVIATION = 2f;

    private final Game game;
    private final Array<Enemy> enemies = new Array<>();
    private final Scheduler removeDeadScheduler;
    private final Array<FormationManager> formations = new Array<>();
    private final RandomXS128 random;
    private boolean shipStarted = false;
    private Phase currentPhase = Phase.WAITING;
    private Scheduler waveScheduler;

    public EnemiesManager(Game game) {
        this.game = game;
        Debug.addButton("Spawn Enemy", () -> {
            Enemy enemy = new RedCreep(game, new Vector2(1.5f, 1.5f));
            game.addEntity(enemy);
            enemies.add(enemy);
        });

        removeDeadScheduler = new Scheduler(this::removeDeadEnemies, 1f, false, true);
        removeDeadScheduler.start();

        MessageManager.getInstance().addListener(this, MessageType.SHIP_STARTED.ordinal());
        random = new RandomXS128();
    }

    public static Structure getClosestStructure(Game game, Vector2 position) {
        Array<Structure> structures = game.map.getStructures();

        Structure target = null;
        float closest = Float.MAX_VALUE;
        for (Structure s : structures) {
            Vector2 sPos = s.getCenter();
            float dist = Vector2.dst(position.x, position.y, sPos.x, sPos.y);
            if (dist < closest) {
                closest = dist;
                target = s;
            }
        }
        return target;
    }

    private void removeDeadEnemies() {
        for (Array.ArrayIterator<Enemy> iterator = enemies.iterator(); iterator.hasNext(); ) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                iterator.remove();
            }
        }
    }

    private void checkEnemies() {
        Vector2 v = new Vector2();
        for (Array.ArrayIterator<FormationManager> iterator = formations.iterator(); iterator.hasNext(); ) {
            FormationManager formation = iterator.next();
            if (!formation.isValid()) {
                iterator.remove();
                continue;
            }
            v.set(formation.getPosition());
            for (Enemy candidate : enemies) {
                if (candidate.getAgent().canJoinFormation()) {
                    if (v.dst(candidate.getCenter()) < Constants.MAKE_FORMATION_RANGE) {
                        MessageManager.getInstance().dispatchMessage(
                                null,
                                candidate.getAgent().getTelegraph(),
                                MessageType.JOIN_FORMATION.ordinal()
                        );
                        try {
                            formation.addMember(candidate.getAgent().getMembership());
                        } catch (IllegalStateException e) {
                            return; // the formation destroyed
                        }
                    }
                }
            }
        }

        // make new formations
        Array<Enemy> candidates = new Array<>(false, 16);
        for (int i = 0; i < enemies.size; i++) {
            Enemy subject = enemies.get(i);
            candidates.clear();
            candidates.add(subject);
            for (int j = i + 1; j < enemies.size; j++) {
                Enemy other = enemies.get(j);
                if (other.getAgent().canJoinFormation()) {
                    v.set(subject.getCenter());
                    if (v.dst(other.getCenter()) < Constants.MAKE_FORMATION_RANGE) {
                        candidates.add(other);
                    }
                }
            }
            if (candidates.size >= Constants.MIN_FORMATION_SIZE) {
                Vector2 startingPoint = candidates.get(0).getCenter(); //TODO
                Structure target = getClosestStructure(game, startingPoint);
                // if no target where found or the target is close, stop making formations
                if (target == null || target.getCenter().dst(startingPoint) < Constants.MAKE_FORMATION_RANGE) {
                    return;
                }
                FormationManager formation = new FormationManager(game, startingPoint, target);
                for (Enemy candidate : candidates) {
                    MessageManager.getInstance().dispatchMessage(
                            null,
                            candidate.getAgent().getTelegraph(),
                            MessageType.JOIN_FORMATION.ordinal()
                    );
                    try {
                        formation.addMember(candidate.getAgent().getMembership());
                    } catch (IllegalStateException e) {
                        return; // the formation destroyed after creation
                    }
                }
                formations.add(formation);
            }
        }
    }

    private void updateFormations(float delta) {
        for (Array.ArrayIterator<FormationManager> iterator = formations.iterator(); iterator.hasNext(); ) {
            FormationManager formation = iterator.next();
            if (formation.isValid())
                formation.update(delta);
            else {
                iterator.remove();
            }
        }
    }

    private float getRandomSpawnTime() {
        // return a random number between 1 and 3 in gaussian distribution
        float rand = (float) (random.nextGaussian() * DEVIATION + MEAN);
        rand = MathUtils.clamp(rand, 5f, 15f);
        return rand;
    }

    private void spawnEnemy(Vector2 position) {
        Enemy enemy = new RedCreep(game, new Vector2(position));
        game.addEntity(enemy);
        enemies.add(enemy);
    }

    private Vector2 getRandomSpawnPosition() {
        int side = MathUtils.random(0, 3);
        Vector2 position;
        switch (side) {
            case 0:
                position = new Vector2(MathUtils.random(1, Constants.MAP_WIDTH - 1), Constants.MAP_HEIGHT);
                break;
            case 1:
                position = new Vector2(MathUtils.random(1, Constants.MAP_WIDTH - 1), 0);
                break;
            case 2:
                position = new Vector2(0, MathUtils.random(1, Constants.MAP_HEIGHT - 1));
                break;
            default:
                position = new Vector2(Constants.MAP_WIDTH, MathUtils.random(1, Constants.MAP_HEIGHT - 1));
        }
        return position;
    }

    private void spawnWave() {
        int waveSize = MathUtils.random(4, 6);
        Vector2 position = getRandomSpawnPosition();

        for (int i = 0; i < waveSize; i++) {
            spawnEnemy(position);
            if (position.x == 0 || position.x == Constants.MAP_WIDTH) {
                position.y += 1f / waveSize;
            } else if (position.y == 0 || position.y == Constants.MAP_HEIGHT) {
                position.x += 1f / waveSize;
            }
        }
        waveScheduler = new Scheduler(this::spawnWave, getRandomSpawnTime());
        waveScheduler.start();
    }

    @Override
    public void update(float deltaTime) {
        if (currentPhase == Phase.WAITING) {
            if (shipStarted) {
                currentPhase = Phase.PHASE_1;
                spawnWave();
            }
        } else if (currentPhase == Phase.PHASE_1) {
            waveScheduler.update(deltaTime);
        }
        removeDeadScheduler.update(deltaTime);
        updateFormations(deltaTime);
        checkEnemies();
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == MessageType.SHIP_STARTED.ordinal()) {
            shipStarted = true;
            return true;
        }
        return false;
    }

    private enum Phase {
        WAITING,
        PHASE_1
    }
}