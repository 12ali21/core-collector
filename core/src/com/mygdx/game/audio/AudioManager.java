package com.mygdx.game.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.utils.AudioAssets;

public class AudioManager implements Disposable {
    public static final float SOUND_DISTANCE_THRESHOLD = 25;

    private final OrthographicCamera camera;
    private final Vector2 cameraPosition = new Vector2();
    private final Array<SpatialSoundEffect> spatialSoundEffects = new Array<>();
    private final Array<NonSpatialSound> simpleSoundEffects = new Array<>();
    private final Array<SpatialMusic> musicSoundEffects = new Array<>();
    private final Array<Music> musics = new Array<>();
    private float soundEffectsVolume = 1f;
    private float musicVolume = 1f;

    public AudioManager(OrthographicCamera camera) {
        this.camera = camera;
    }

    private Sound loadSound(AudioAssets assets) {
        return AudioAssets.getSound(assets);
    }

    private Music loadMusic(AudioAssets assets) {
        return AudioAssets.getMusic(assets);
    }

    public SpatialSoundNonLooping newNonLoopingSpatialSoundEffect(AudioAssets assets, float volumeOffset) {
        SpatialSoundNonLooping soundEffect = new SpatialSoundNonLooping(loadSound(assets));
        soundEffect.setVolumeOffset(volumeOffset);
        soundEffect.setGlobalVolume(soundEffectsVolume);
        spatialSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public SpatialSoundLooping newLoopingSpatialSoundEffect(AudioAssets assets, float volumeOffset) {
        SpatialSoundLooping soundEffect = new SpatialSoundLooping(loadSound(assets));
        soundEffect.setVolumeOffset(volumeOffset);
        soundEffect.setGlobalVolume(soundEffectsVolume);
        spatialSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public NonSpatialSound newNonSpatialSoundEffect(AudioAssets assets, float volumeOffset) {
        NonSpatialSound soundEffect = new NonSpatialSound(loadSound(assets));
        soundEffect.setVolumeOffset(volumeOffset);
        soundEffect.setGlobalVolume(soundEffectsVolume);
        simpleSoundEffects.add(soundEffect);
        return soundEffect;
    }

    public Music newMusic(AudioAssets assets) {
        Music music = loadMusic(assets);
        music.setVolume(musicVolume);
        return music;
    }

    public SpatialMusic newSpatialMusicSFX(AudioAssets assets, float volumeOffset) {
        Music music = loadMusic(assets);
        SpatialMusic spatialMusic = new SpatialMusic(music);
        spatialMusic.setVolumeOffset(volumeOffset);
        spatialMusic.setGlobalVolume(soundEffectsVolume);
        musics.add(music);
        musicSoundEffects.add(spatialMusic);
        return spatialMusic;
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

        for (SpatialMusic spatialMusic : musicSoundEffects) {
            if (spatialMusic.isValid()) {
                spatialMusic.setCameraPosition(cameraPosition);
                spatialMusic.setCameraZoom(camera.zoom);
                spatialMusic.update(deltaTime);
            } else {
                musicSoundEffects.removeValue(spatialMusic, true);
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

        for (SpatialMusic music : musicSoundEffects) {
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

        for (SpatialMusic music : musicSoundEffects) {
            music.setGlobalVolume(musicVolume);
        }
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        for (Music music : musics) {
            music.setVolume(musicVolume);
        }
    }
}
