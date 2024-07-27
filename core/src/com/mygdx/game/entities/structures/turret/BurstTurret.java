package com.mygdx.game.entities.structures.turret;

import com.mygdx.game.utils.Scheduler;
import com.mygdx.game.world.Game;

public class BurstTurret extends Turret {
    private final int burstCount;
    private final Scheduler burstScheduler;
    private int currentBurst;
    private boolean onBurst;

    //TODO: Reload burst when lost sight of target
    protected BurstTurret(Builder builder) {
        super(builder);
        this.burstCount = builder.burstCount;
        currentBurst = burstCount;
        burstScheduler = new Scheduler(() -> onBurst = true, builder.burstCooldown, false, true);
        burstScheduler.start();
    }

    @Override
    protected boolean shoot(float delta) {
        if (onBurst) {
            if (super.shoot(delta)) {
                currentBurst--;
                if (currentBurst <= 0) {
                    onBurst = false;
                    currentBurst = burstCount;
                }
                return true;
            }
        } else {
            burstScheduler.update(delta);
        }
        return false;
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
