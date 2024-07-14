package com.mygdx.game.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Turret extends Entity {

    private final TurretHead head;
    private final TurretBase base;

    public Turret(AssetManager assets, Batch batch, Vector2 position, TurretHead head, TurretBase base) {
        super(assets, batch, position);
        this.head = head;
        this.base = base;
    }

    @Override
    public void render() {
        base.render();
        head.render();
    }

    public void setTarget(float x, float y) {
        head.setTarget(x, y);
    }

    @Override
    public void update(float deltaTime) {
        head.update(deltaTime);
    }

    public static Turret BasicTurret(AssetManager assets, Batch batch, Vector2 position) {
        TurretHead head = new TurretHead(assets, batch, position);
        TurretBase base = new TurretBase(assets, batch, position);
        return new Turret(assets, batch, position, head, base);
    }
}
