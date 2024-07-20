package com.mygdx.game.entities;

public class HealthPoints {
    private final float maxHp;
    private float hp;

    private final Callback callback;

    public HealthPoints(float maxHp, Callback callback) {
        this.maxHp = maxHp;
        this.callback = callback;
        hp = maxHp;
    }

    public void damage(float amount) {
        hp -= amount;
        if (hp <= 0) {
            callback.onDeath();
        }
    }

    public void heal(float amount) {
        hp += amount;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getHp() {
        return hp;
    }

    public interface Callback {
        void onDeath();
    }
}
