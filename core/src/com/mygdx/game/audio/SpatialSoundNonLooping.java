package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.utils.Constants;

public class SpatialSoundNonLooping extends SpatialSoundEffect {
    Array<Long> ids = new Array<>();
    Array<Float> accumulators = new Array<>();

    public SpatialSoundNonLooping(Sound sound) {
        super(sound);
    }

    @Override
    public void play() {
        long id = sound.play();
        ids.add(id);
        accumulators.add(0f);
    }

    @Override
    public void stop() {
        for (int i = 0; i < ids.size; i++) {
            sound.stop(ids.get(i));
        }
        ids.clear();
        accumulators.clear();
    }

    @Override
    public void pause() {
        for (int i = 0; i < ids.size; i++) {
            sound.pause(ids.get(i));
        }
    }

    @Override
    public void resume() {
        for (int i = 0; i < ids.size; i++) {
            sound.resume(ids.get(i));
        }
    }

    public void update(float deltaTime) {
        updateAccumulators(deltaTime);
        for (int i = 0; i < ids.size; i++) {
            sound.setPan(ids.get(i), calculatePan(), calculateVolume());
        }
    }

    private void updateAccumulators(float delta) {
        for (int i = 0; i < accumulators.size; i++) {
            float x = accumulators.get(i) + delta;
            if (x > Constants.SOUND_EFFECT_LIMIT) {
                accumulators.removeIndex(i);
                ids.removeIndex(i);
                continue;
            }
            accumulators.set(i, x);
        }
    }
}
