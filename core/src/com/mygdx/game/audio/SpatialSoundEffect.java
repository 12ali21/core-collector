package com.mygdx.game.audio;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Updatable;

public abstract class SpatialSoundEffect extends SoundEffect implements Updatable {
    Vector2 position = new Vector2();
    Vector2 cameraPosition = new Vector2();
    float cameraZoom;


    public SpatialSoundEffect(Sound sound) {
        super(sound);
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

    public void setCameraPosition(Vector2 cameraPosition) {
        this.cameraPosition.set(cameraPosition);
    }

    public void setCameraZoom(float cameraZoom) {
        this.cameraZoom = cameraZoom;
    }
}
