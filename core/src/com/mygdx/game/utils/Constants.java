package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Constants {
    public static final Skin SKIN = new Skin(Gdx.files.internal("ui/skin-ui.json"));
    public static final boolean DEBUG = true;
    public static final float VIEWPORT_SIZE = 50;
    public static final float ZOOM_LIMIT_LOW = 0.1f;
    public static final float ZOOM_LIMIT_HIGH = 3f;
    public static final float ZOOM_CHANGE_SPEED = 0.1f;
    public static final float PAN_SPEED = 15f;
    public static final float SOUND_EFFECT_LIMIT = 60f; // 1 min
    public static final int MAP_BORDER_LENGTH = 2;

    public static final float MAKE_FORMATION_RANGE = 5f;
    public static final int MIN_FORMATION_SIZE = 4;
    public static final float BREAK_FORMATION_RANGE = 5f;
    public static final float AGGRO_RANGE = BREAK_FORMATION_RANGE;

    public static final int SHIP_SIZE = 16;
}
