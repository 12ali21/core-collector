package com.mygdx.game.entities.turret;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.entities.Structure;
import com.mygdx.game.entities.World;

public class Turrets {
    public static Turret.Builder basicTurret(World world, int x, int y) {

        Texture t = world.getAssets().get("sprites/turret_base.png", Texture.class);

        Sprite baseSprite = new Sprite(t);
        baseSprite.setSize(2f, 2f);
        baseSprite.setOrigin(baseSprite.getWidth() / 2, baseSprite.getHeight() / 2);
        baseSprite.setOriginBasedPosition(x, y);
        baseSprite.setScale(0.75f);

        t = world.getAssets().get("sprites/turret_head.png", Texture.class);
        Sprite headSprite = new Sprite(t);
        headSprite.setSize(2, 2);
        headSprite.setOrigin(0.5f, headSprite.getHeight() / 2);
        headSprite.setOriginBasedPosition(x, y);


        return new Turret.Builder(world)
                .setBase(new Structure.StructurePart(world, baseSprite))
                .setHead(new Structure.StructurePart(world, headSprite))
                .setRotationSpeed(30f)
                .setBulletSpeed(20f)
                .setFireRate(80)
                .setBounds(x, y, 2, 2);
    }
}
