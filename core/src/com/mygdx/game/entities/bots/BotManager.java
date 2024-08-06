package com.mygdx.game.entities.bots;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Updatable;
import com.mygdx.game.world.Game;

public class BotManager implements Updatable {
    private final Array<Bot> bots = new Array<>();

    public BotManager(Game game, int botCount) {
        Vector2 pos = new Vector2(game.getShip().getCenter());
        for (int i = 0; i < botCount; i++) {
            Bot bot = (Bot) game.entities.addEntity(new Bot(game, pos, game.getShip()));
            bots.add(bot);
        }
    }

    @Override
    public void update(float deltaTime) {

    }
}
