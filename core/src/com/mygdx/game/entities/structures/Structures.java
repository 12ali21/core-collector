package com.mygdx.game.entities.structures;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.entities.structures.turret.BurstTurret;
import com.mygdx.game.entities.structures.turret.Turret;
import com.mygdx.game.utils.TextureAssets;
import com.mygdx.game.world.Game;

public class Structures {
    private static Structure.StructurePart makeHead(Game game, TextureAssets assets, int x, int y) {
        Sprite headSprite = new Sprite(TextureAssets.get(assets));
        headSprite.setSize(2, 2);
        headSprite.setOrigin(0.5f, headSprite.getHeight() / 2);
        headSprite.setOriginBasedPosition(x, y);
        return new Structure.StructurePart(game, headSprite);
    }

    private static Structure.StructurePart makeBase(Game game, TextureAssets assets, int x, int y) {
        Sprite baseSprite = new Sprite(TextureAssets.get(assets));
        baseSprite.setSize(2f, 2f);
        baseSprite.setOrigin(baseSprite.getWidth() / 2, baseSprite.getHeight() / 2);
        baseSprite.setOriginBasedPosition(x, y);
        baseSprite.setScale(0.75f);
        return new Structure.StructurePart(game, baseSprite);
    }

    public static Turret.Builder basicTurret(Game game, int x, int y) {
        Turret.Builder builder = new Turret.Builder(game, 100);
        builder.setBase(makeBase(game, TextureAssets.TURRET_BASE_TEXTURE, x, y));
        builder.setHead(makeHead(game, TextureAssets.TURRET_HEAD_TEXTURE, x, y));
        builder.setRotationSpeed(90f);
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
        builder.setBase(makeBase(game, TextureAssets.TURRET_BASE_TEXTURE, x, y));
        builder.setHead(makeHead(game, TextureAssets.TURRET_HEAD_MULTI_TEXTURE, x, y));
        builder.setRotationSpeed(90f);
        builder.setBulletSpeed(50f);
        builder.setFireRate(360);
        builder.setBounds(x, y);
        builder.setRangeRadius(20f);
        return builder;
    }

    public static Ship.Builder ship(Game game, int x, int y) {
        Sprite body = new Sprite(TextureAssets.get(TextureAssets.SHIP_TEXTURE));
        Ship.Builder builder = new Ship.Builder(game);
        builder.setMainPart(new Structure.StructurePart(game, body));
        builder.setBounds(x, y);
        return builder;
    }
}
