package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Sound;

public class SpatialSoundLooping extends SpatialSoundEffect {
    private long id = -1;

    public SpatialSoundLooping(Sound sound) {
        super(sound);
    }

    @Override
    public void play() {
        id = sound.play();
        sound.setLooping(id, true);
    }

    @Override
    public void stop() {
        sound.stop(id);
        id = -1;
    }

    @Override
    public void pause() {
        sound.pause(id);
    }

    @Override
    public void resume() {
        if (id == -1) {
            play();
        } else {
            sound.resume(id);
        }
    }

    @Override
    public void update(float deltaTime) {
        sound.setPan(id, calculatePan(), calculateVolume());
    }
}
