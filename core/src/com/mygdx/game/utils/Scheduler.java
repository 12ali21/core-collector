package com.mygdx.game.utils;

public class Scheduler {
    public final SchedulerCallback callback;
    public final float interval;
    public final boolean continuous;
    public final boolean repeat;

    private float accumulator;
    private boolean running;

    public Scheduler(SchedulerCallback callback, float interval, boolean continuous, boolean repeat) {
        this.callback = callback;
        this.interval = interval;
        this.continuous = continuous;
        this.repeat = repeat;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
        accumulator = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public void update(float delta) {
        if (!running) {
            return;
        }

        accumulator += delta;
        if (accumulator >= interval) {
            callback.run();
            if (repeat) {
                accumulator -= interval;
            } else {
                stop();
            }
        } else if (continuous) {
            callback.run();
        }
    }

    public interface SchedulerCallback {
        void run();
    }
}
