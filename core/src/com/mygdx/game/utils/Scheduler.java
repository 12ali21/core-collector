package com.mygdx.game.utils;

public class Scheduler {
    public final SchedulerCallback callback;
    public final float interval;
    public final boolean continuous;
    public final boolean repeat;
    public int repeatCount = -1; // infinite
    public int currRepeatCount;

    private float accumulator;
    private boolean running;

    public Scheduler(SchedulerCallback callback, float interval) {
        this(callback, interval, false, false);
    }

    public Scheduler(SchedulerCallback callback, float interval, boolean continuous, boolean repeat) {
        this.callback = callback;
        this.interval = interval;
        this.continuous = continuous;
        this.repeat = repeat;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
        currRepeatCount = repeatCount;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
        accumulator = 0;
        if (repeatCount != -1) {
            currRepeatCount = repeatCount;
        }
    }

    public void resetAccumulator() {
        accumulator = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean update(float delta) {
        if (!running) {
            return false;
        }

        accumulator += delta;
        if (accumulator >= interval) {
            callback.run();
            if (repeat) {
                if (repeatCount != -1) {
                    currRepeatCount--;
                    if (currRepeatCount > 0) {
                        accumulator -= interval;
                    } else {
                        stop();
                    }
                } else {
                    accumulator -= interval;
                }
            } else {
                stop();
            }
            return true;
        } else if (continuous) {
            callback.run();
            return true;
        }
        return false;
    }

    public interface SchedulerCallback {
        void run();
    }
}
