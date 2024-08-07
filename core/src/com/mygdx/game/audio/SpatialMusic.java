package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Updatable;

public class SpatialMusic implements Disposable, Updatable {
    private final Music music;
    private final Vector2 position = new Vector2();
    private final Vector2 cameraPosition = new Vector2();
    private float cameraZoom;
    private float globalVolume = 1f;
    private float volumeOffset = 1f;
    private float volume = 1f;
    private boolean isValid = true;

    public SpatialMusic(Music music) {
        this.music = music;
    }

    protected float calculatePan() {
        return AudioUtils.calculatePan(position, cameraPosition);
    }

    protected float calculateVolume() {
        return AudioUtils.calculateVolume(position, cameraPosition, cameraZoom, volume);
    }

    public void setPosition(Vector2 position) {
        this.position.set(position);
    }

    public void setCameraPosition(Vector2 position) {
        this.cameraPosition.set(position);
    }

    public void setCameraZoom(float zoom) {
        cameraZoom = zoom;
    }

    public void setVolumeOffset(float volumeOffset) {
        this.volumeOffset = volumeOffset;
        this.volume = volumeOffset * globalVolume;
    }

    public void setGlobalVolume(float volume) {
        globalVolume = volume;
        this.volume = volumeOffset * globalVolume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public Music getMusic() {
        return music;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public void update(float deltaTime) {
        if (isValid) {
            music.setPan(calculatePan(), calculateVolume());
        }
    }

    @Override
    public void dispose() {
        isValid = false;
        music.dispose();
    }
}
