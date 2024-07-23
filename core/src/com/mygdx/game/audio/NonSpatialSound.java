package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Sound;

public class NonSpatialSound extends SoundEffect {
    private long id;

    public NonSpatialSound(Sound sound) {
        super(sound);
    }

    @Override
    public void play() {
        id = sound.play();
    }

    @Override
    public void stop() {
        sound.stop(id);
    }

    @Override
    public void pause() {
        sound.pause();
    }

    @Override
    public void resume() {
        sound.resume();
    }

    @Override
    public void setGlobalVolume(float globalVolume) {
        super.setGlobalVolume(globalVolume);
        sound.setVolume(id, volume * globalVolume);
    }
}
