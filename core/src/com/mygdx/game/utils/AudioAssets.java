package com.mygdx.game.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public enum AudioAssets {
    CANON_SHOOT("audio/soundeffects/turret_shoot.wav", false),
    PAUSE_SOUND("audio/soundeffects/pause.wav", false),
    TURRET_ROTATE("audio/soundeffects/turret_rotate.wav", false),
    SHIP_START("audio/soundeffects/miner_start.wav", true),
    SHIP_MINE("audio/soundeffects/miner_mine.wav", false),
    AMBIENT_MUSIC("audio/music/soft_wind.wav", true),
    PRE_MINE_MUSIC_1("audio/music/Suspensify.wav", true),
    POST_MINE_MUSIC_1("audio/music/Hitman.mp3", true),
    POST_MINE_MUSIC_2("audio/music/Harmful or Fatal.mp3", true);

    private static AssetManager manager;

    final String path;
    final boolean isMusic;

    AudioAssets(String path, boolean isMusic) {
        this.path = path;
        this.isMusic = isMusic;
    }

    public static void loadAll(AssetManager manager) {
        for (AudioAssets asset : AudioAssets.values()) {
            if (asset.isMusic) {
                manager.load(asset.path, Music.class);
            } else {
                manager.load(asset.path, Sound.class);
            }
        }
        AudioAssets.manager = manager;
    }

    public static void unloadAll(AssetManager manager) {
        for (AudioAssets asset : AudioAssets.values()) {
            manager.unload(asset.path);
        }
        AudioAssets.manager = null;
    }

    public static Music getMusic(AudioAssets asset) {
        return manager.get(asset.path, Music.class);
    }

    public static Sound getSound(AudioAssets asset) {
        return manager.get(asset.path, Sound.class);
    }
}
