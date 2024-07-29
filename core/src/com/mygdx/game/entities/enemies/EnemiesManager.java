package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.ai.msg.MessageManager;
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

public class EnemiesManager implements Updatable {

    private final Game game;
    private final Array<Enemy> enemies = new Array<>();
    private final Scheduler removeDeadScheduler;
    private final Array<FormationManager> formations = new Array<>();

    public EnemiesManager(Game game) {
        this.game = game;
        Debug.addButton("Spawn Enemy", () -> {
            Enemy enemy = new RedCreep(game, new Vector2(1.5f, 1.5f));
            game.addEntity(enemy);
            enemies.add(enemy);
        });

        removeDeadScheduler = new Scheduler(this::removeDeadEnemies, 1f, false, true);
        removeDeadScheduler.start();
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
                        formation.addMember(candidate.getAgent().getMembership());
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
                if (target == null) return; // if no target where found, stop making formations
                FormationManager formation = new FormationManager(game, startingPoint, target);
                for (Enemy candidate : candidates) {
                    MessageManager.getInstance().dispatchMessage(
                            null,
                            candidate.getAgent().getTelegraph(),
                            MessageType.JOIN_FORMATION.ordinal()
                    );
                    formation.addMember(candidate.getAgent().getMembership());
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

    @Override
    public void update(float deltaTime) {
        removeDeadScheduler.update(deltaTime);
        updateFormations(deltaTime);
        checkEnemies();
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }
}