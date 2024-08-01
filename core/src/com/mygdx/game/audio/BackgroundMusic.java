package com.mygdx.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.Updatable;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.world.Game;

public class BackgroundMusic implements Updatable, Telegraph {
    private final Game game;
    private final StateMachine<BackgroundMusic, MusicState> stateMachine;
    private final float FADE_OUT_SPEED = 0.1f;
    private MusicState currentState;
    private Music currMusic;
    private float fadeOutVol;

    public BackgroundMusic(Game game) {
        this.game = game;
        stateMachine = new DefaultStateMachine<>(this, MusicState.INITIAL);
        currentState = MusicState.MINING;
        MessageManager.getInstance().addListener(stateMachine, MessageType.SHIP_STARTED.ordinal());

        Music ambient = game.audio.newMusic(AudioAssets.AMBIENT_MUSIC);
        ambient.setLooping(true);
        ambient.setVolume(0.4f);
        ambient.play();
    }

    private void chooseMineMusic() {
        int rand = MathUtils.random(0, 1);
        if (rand == 0) {
            currMusic = game.audio.newMusic(AudioAssets.POST_MINE_MUSIC_1);
        } else {
            currMusic = game.audio.newMusic(AudioAssets.POST_MINE_MUSIC_2);
        }
        currMusic.play();
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }

    @Override
    public void update(float deltaTime) {
        stateMachine.update();
    }

    private enum MusicState implements State<BackgroundMusic> {
        INITIAL() {
            @Override
            public void update(BackgroundMusic entity) {
                if (entity.currMusic == null) {
                    entity.currMusic = entity.game.audio.newMusic(AudioAssets.PRE_MINE_MUSIC_1);
                    entity.currMusic.setLooping(true);
                    entity.currMusic.setVolume(entity.currMusic.getVolume() * 0.4f);
                    entity.currMusic.play();
                }
            }
        },
        FADING_OUT() {
            @Override
            public void enter(BackgroundMusic entity) {
                entity.fadeOutVol = entity.currMusic.getVolume();
            }

            @Override
            public void update(BackgroundMusic entity) {
                if (!entity.currMusic.isPlaying()) {// fading out finished
                    entity.stateMachine.changeState(entity.currentState);
                } else {
                    entity.fadeOutVol -= entity.FADE_OUT_SPEED * Gdx.graphics.getDeltaTime();
                    if (entity.fadeOutVol > 0) {
                        entity.currMusic.setVolume(entity.fadeOutVol);
                    } else {
                        entity.currMusic.stop();
                    }
                }
            }
        },
        MINING() {
            @Override
            public void update(BackgroundMusic entity) {
                if (!entity.currMusic.isPlaying()) {
                    entity.chooseMineMusic();
                }
            }
        };

        @Override
        public void enter(BackgroundMusic entity) {

        }

        @Override
        public void update(BackgroundMusic entity) {

        }

        @Override
        public void exit(BackgroundMusic entity) {

        }

        @Override
        public boolean onMessage(BackgroundMusic entity, Telegram telegram) {
            if (telegram.message == MessageType.SHIP_STARTED.ordinal()) {
                entity.currentState = MINING;
                entity.stateMachine.changeState(FADING_OUT);
                return true;
            }
            return false;
        }
    }
}
