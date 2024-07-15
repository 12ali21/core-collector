package com.mygdx.game.entities.turret;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.World;
import com.mygdx.game.entities.Entity;

public class Turret extends Entity {

    private final TurretHead head;
    private final TurretBase base;

    public Turret(World world, Vector2 position, TurretHead head, TurretBase base) {
        super(world, position);
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

    public static Turret BasicTurret(World world, Vector2 position) {
        TurretHead head = new TurretHead(world, position);
        TurretBase base = new TurretBase(world, position);
        return new Turret(world, position, head, base);
    }
}
