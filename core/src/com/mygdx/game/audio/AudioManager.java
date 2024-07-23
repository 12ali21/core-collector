package com.mygdx.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AudioManager implements Disposable {
    public static final float SOUND_DISTANCE_THRESHOLD = 25;

    private final OrthographicCamera camera;
    private final Vector2 cameraPosition = new Vector2();
    private final Array<SpatialSoundEffect> spatialSoundEffects = new Array<>();
    private final Array<NonSpatialSound> simpleSoundEffects = new Array<>();
    private final Array<Music> musics = new Array<>();
    private float soundEffectsVolume = 1f;
    private float musicVolume = 1f;

    public AudioManager(OrthographicCamera camera) {
        this.camera = camera;
    }

    private Sound loadSound(String path) {
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    private Music loadMusic(String path) {
        return Gdx.audio.newMusic(Gdx.files.internal(path));
    }

    public SpatialSoundNonLooping newNonLoopingSpatialSoundEffect(String path) {
        SpatialSoundNonLooping soundEffect = new SpatialSoundNonLooping(loadSound(path));
        soundEffect.setGlobalVolume(soundEffectsVolume);
        spatialSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public SpatialSoundLooping newLoopingSpatialSoundEffect(String path) {
        SpatialSoundLooping soundEffect = new SpatialSoundLooping(loadSound(path));
        soundEffect.setGlobalVolume(soundEffectsVolume);
        spatialSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public NonSpatialSound newNonSpatialSoundEffect(String path) {
        NonSpatialSound soundEffect = new NonSpatialSound(loadSound(path));
        soundEffect.setGlobalVolume(soundEffectsVolume);
        simpleSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public Music newMusic(String path) {
        Music music = loadMusic(path);
        music.setVolume(musicVolume);
        return music;
    }

    public void update(float deltaTime) {
        cameraPosition.set(camera.position.x, camera.position.y);
        for (SpatialSoundEffect spatialSoundEffect : spatialSoundEffects) {
            if (spatialSoundEffect.isValid()) {
                spatialSoundEffect.setCameraPosition(cameraPosition);
                spatialSoundEffect.setCameraZoom(camera.zoom);
                spatialSoundEffect.update(deltaTime);
            } else {
                spatialSoundEffects.removeValue(spatialSoundEffect, true);
            }
        }
    }

    @Override
    public void dispose() {
        for (SoundEffect soundEffect : spatialSoundEffects) {
            soundEffect.sound.dispose();
            soundEffect.dispose();
        }

        for (SoundEffect soundEffect : simpleSoundEffects) {
            soundEffect.sound.dispose();
            soundEffect.dispose();
        }

        for (Music music : musics) {
            music.dispose();
        }
    }

    public void setSoundEffectsVolume(float globalVolume) {
        this.soundEffectsVolume = globalVolume;
        for (SoundEffect soundEffect : spatialSoundEffects) {
            soundEffect.setGlobalVolume(globalVolume);
        }

        for (SoundEffect soundEffect : simpleSoundEffects) {
            soundEffect.setGlobalVolume(globalVolume);
        }
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        for (Music music : musics) {
            music.setVolume(musicVolume);
        }
    }
}
