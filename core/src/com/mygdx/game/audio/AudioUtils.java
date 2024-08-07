package com.mygdx.game.audio;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.utils.Constants;

public class AudioUtils {
    public static float calculatePan(Vector2 position, Vector2 cameraPosition) {
        float pan = (position.x - cameraPosition.x) / AudioManager.SOUND_DISTANCE_THRESHOLD;
        return Math.min(1, Math.max(-1, pan));
    }

    public static float calculateVolume(Vector2 position, Vector2 cameraPosition, float cameraZoom, float intrinsicVolume) {
        float distance = position.dst(cameraPosition);
        float volume = 1 - distance / AudioManager.SOUND_DISTANCE_THRESHOLD;
        float zoomScale = 1f / (cameraZoom / Constants.ZOOM_LIMIT_LOW); // normalize zoom
        volume *= zoomScale;
        return Math.min(1, Math.max(0, volume)) * intrinsicVolume;
    }
}
