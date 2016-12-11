package com.stirante.asem.utils;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stirante
 */
public abstract class AsyncTask<P, T, R> {

    private AtomicBoolean cancelled = new AtomicBoolean(false);
    private AtomicBoolean done = new AtomicBoolean(false);

    @SafeVarargs
    public final void execute(P... params) {
        new Thread() {
            @Override
            public void run() {
                R result = doInBackground(params);
                done.set(true);
                Platform.runLater(() -> onPostExecute(result));
            }
        }.start();
    }

    public void onProgress(T progress) {

    }

    public final void publishProgress(T progress) {
        Platform.runLater(() -> onProgress(progress));
    }

    public abstract R doInBackground(P[] params);

    public void onPostExecute(R result) {

    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isDone() {
        return done.get();
    }

    public void cancel() {
        cancelled.set(true);
    }

}
