package com.mygdx.game.ai.agents;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.utils.Bodies;
import com.mygdx.game.world.Game;

public class BotAgent extends Agent {
    public BotAgent(Game game, Vector2 position) {
        super(game, Bodies.createBotBody(game, position));
    }
}
