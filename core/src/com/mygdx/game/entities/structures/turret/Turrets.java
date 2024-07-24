package com.mygdx.game.entities.structures.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.entities.structures.Structure;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.world.Game;

public class Turrets {
    public static Turret.Builder basicTurret(Game game, int x, int y) {

        Texture t = game.getAssets().get(Constants.TURRET_BASE_TEXTURE, Texture.class);

        Sprite baseSprite = new Sprite(t);
        baseSprite.setSize(2f, 2f);
        baseSprite.setOrigin(baseSprite.getWidth() / 2, baseSprite.getHeight() / 2);
        baseSprite.setOriginBasedPosition(x, y);
        baseSprite.setScale(0.75f);

        t = game.getAssets().get(Constants.TURRET_HEAD_TEXTURE, Texture.class);
        Sprite headSprite = new Sprite(t);
        headSprite.setSize(2, 2);
        headSprite.setOrigin(0.5f, headSprite.getHeight() / 2);
        headSprite.setOriginBasedPosition(x, y);


        return new Turret.Builder(game, 100)
                .setBase(new Structure.StructurePart(game, baseSprite))
                .setHead(new Structure.StructurePart(game, headSprite))
                .setRotationSpeed(30f)
                .setBulletSpeed(30f)
                .setFireRate(80)
                .setBounds(x, y);
    }
}
