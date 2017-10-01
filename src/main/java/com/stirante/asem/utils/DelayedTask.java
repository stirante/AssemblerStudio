package com.stirante.asem.utils;

import java.util.concurrent.atomic.AtomicLong;

public class DelayedTask extends AsyncTask<Void, Void, Void> {

    private final long delay;
    private Runnable runnable;
    private AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    public DelayedTask(long delay) {
        this.delay = delay;
    }

    public void start(Runnable r) {
        startTime.set(System.currentTimeMillis());
        this.runnable = r;
        execute();
    }

    @Override
    public Void doInBackground(Void[] params) {
        long timeToWait = delay - (System.currentTimeMillis() - startTime.get());
        while (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeToWait = delay - (System.currentTimeMillis() - startTime.get());
        }
        return null;
    }

    @Override
    public void onPostExecute(Void result) {
        runnable.run();
    }
}
