package com.mygdx.game.entities.structures.turret;

import com.mygdx.game.world.Game;

public class BurstTurret extends Turret {
    private final float burstCooldown;
    private final int burstCount;
    private float burstTimer;
    private int currentBurst;

    private boolean onBurst;

    //TODO: Reload burst when lost sight of target
    protected BurstTurret(Builder builder) {
        super(builder);
        this.burstCooldown = builder.burstCooldown;
        this.burstCount = builder.burstCount;
        currentBurst = burstCount;
    }

    @Override
    protected boolean fire(float delta) {
        if (onBurst) {
            if (super.fire(delta)) {
                currentBurst--;
                if (currentBurst <= 0) {
                    onBurst = false;
                    currentBurst = burstCount;
                }
                return true;
            }
        } else {
            burstTimer -= delta;
            if (burstTimer <= 0) {
                onBurst = true;
                burstTimer = burstCooldown;
            }
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
