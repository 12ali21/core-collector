package com.mygdx.game.entities.structures.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.world.Game;

public class Turrets {
    private static Structure.StructurePart makeHead(Game game, String path, int x, int y) {
        Sprite headSprite = new Sprite(game.getAssets().get(path, Texture.class));
        headSprite.setSize(2, 2);
        headSprite.setOrigin(0.5f, headSprite.getHeight() / 2);
        headSprite.setOriginBasedPosition(x, y);
        return new Structure.StructurePart(game, headSprite);
    }

    private static Structure.StructurePart makeBase(Game game, String path, int x, int y) {
        Sprite baseSprite = new Sprite(game.getAssets().get(path, Texture.class));
        baseSprite.setSize(2f, 2f);
        baseSprite.setOrigin(baseSprite.getWidth() / 2, baseSprite.getHeight() / 2);
        baseSprite.setOriginBasedPosition(x, y);
        baseSprite.setScale(0.75f);
        return new Structure.StructurePart(game, baseSprite);
    }

    public static Turret.Builder basicTurret(Game game, int x, int y) {
        Turret.Builder builder = new Turret.Builder(game, 100);
        builder.setBase(makeBase(game, Constants.TURRET_BASE_TEXTURE, x, y));
        builder.setHead(makeHead(game, Constants.TURRET_HEAD_TEXTURE, x, y));
        builder.setRotationSpeed(30f);
        builder.setBulletSpeed(30f);
        builder.setFireRate(80);
        builder.setBounds(x, y);
        builder.setRangeRadius(15);
        return builder;
    }

    public static BurstTurret.Builder burstTurret(Game game, int x, int y) {
        BurstTurret.Builder builder = new BurstTurret.Builder(game, 200);
        builder.setBurstCooldown(1.5f);
        builder.setBurstCount(3);
        builder.setBase(makeBase(game, Constants.TURRET_BASE_TEXTURE, x, y));
        builder.setHead(makeHead(game, Constants.TURRET_HEAD_MULTI_TEXTURE, x, y));
        builder.setRotationSpeed(30f);
        builder.setBulletSpeed(50f);
        builder.setFireRate(360);
        builder.setBounds(x, y);
        builder.setRangeRadius(20f);
        return builder;
    }
}
