package com.mygdx.game.entities.enemies;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.mygdx.game.Updatable;
import com.mygdx.game.utils.Scheduler;

public class EnemiesManager implements Updatable {

    private final Scheduler scheduler;

    public EnemiesManager() {
        scheduler = new Scheduler(() -> {
            MessageManager.getInstance().dispatchMessage(1);
            System.out.println("dispatched");
        }, 4);
        scheduler.start();
    }

    @Override
    public void update(float deltaTime) {
        scheduler.update(deltaTime);
    }
}
