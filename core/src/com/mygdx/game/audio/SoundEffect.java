package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public abstract class SoundEffect implements Disposable {
    protected final Sound sound;
    protected boolean valid = true;
    protected float volume = 1f;
    protected float volumeOffset = 1f;
    protected float globalVolume = 1f;

    public SoundEffect(Sound sound) {
        this.sound = sound;
    }

    public abstract void play();

    public abstract void stop();

    public abstract void pause();

    public abstract void resume();

    public void setVolumeOffset(float volume) {
        this.volumeOffset = volume;
        this.volume = volumeOffset * globalVolume;
    }

    void setGlobalVolume(float volume) {
        this.globalVolume = volume;
        this.volume = volumeOffset * globalVolume;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public void dispose() {
        valid = false;
        stop();
    }
}
