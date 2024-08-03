package com.mygdx.game.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public enum TextureAssets {
    BOT_TEXTURE("sprites/bot.png"),
    BULLET_TEXTURE("sprites/bullet.png"),
    ENEMY_BIG_TEXTURE("sprites/enemy_big.png"),
    ENEMY_SMALL_TEXTURE("sprites/enemy_small.png"),
    HOVERED_TILE_TEXTURE("sprites/hovered_tile.png"),
    SELECTED_TILE_TEXTURE("sprites/selected_tile.png"),
    SHIP_TEXTURE("sprites/ship.png"),
    TURRET_BASE_TEXTURE("sprites/turret_base.png"),
    TURRET_HEAD_TEXTURE("sprites/turret_head.png"),
    TURRET_HEAD_MULTI_TEXTURE("sprites/turret_head_multi.png"),
    GROUND_TEXTURE("maps/ground/1.png"),
    HEALTH_BORDER_TEXTURE("sprites/health_border.png"),
    GREEN_BAR_TEXTURE("sprites/green_bar.png"),
    YELLOW_BAR_TEXTURE("sprites/yellow_bar.png"),
    RED_BAR_TEXTURE("sprites/red_bar.png"),
    DIRT_WALL_TEXTURE("maps/tiles/Walls/Wall-Dirt_02-64x64.png"),
    BUILDING_ICON("sprites/building_icon.png");

    static AssetManager manager;
    final String path;

    TextureAssets(String path) {
        this.path = path;
    }

    public static void loadAll(AssetManager assets) {
        for (TextureAssets asset : TextureAssets.values()) {
            assets.load(asset.path, Texture.class);
        }
        TextureAssets.manager = assets;
    }

    public static void unloadAll(AssetManager assets) {
        for (TextureAssets asset : TextureAssets.values()) {
            assets.unload(asset.path);
        }
        TextureAssets.manager = null;
    }

    public static Texture get(TextureAssets asset) {
        return manager.get(asset.path, Texture.class);
    }
}
