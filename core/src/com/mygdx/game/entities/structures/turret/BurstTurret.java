package com.mygdx.game.entities.structures.turret;

import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.world.Game;

public class BurstTurret extends Turret {
    private final Scheduler burstFireScheduler;

    protected BurstTurret(Builder builder) {
        super(builder);
        burstFireScheduler = new Scheduler(super::fire, builder.burstCooldown, false, true);
        burstFireScheduler.setRepeatCount(builder.burstCount);
    }

    @Override
    protected void fire() {
        if (!burstFireScheduler.isRunning()) {
            burstFireScheduler.start();
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        burstFireScheduler.update(deltaTime);
    }

    public static class Builder extends Turret.Builder {
        private float burstCooldown;
        private int burstCount;

        public Builder(Game game, float hitPoints) {
            super(game, hitPoints);
        }

        public void setBurstCooldown(float cooldown) {
            this.burstCooldown = cooldown;
        }

        public void setBurstCount(int burstCount) {
            this.burstCount = burstCount;
        }

        @Override
        public BurstTurret build() {
            return new BurstTurret(this);
        }
    }
}
