package com.stirante.asem.utils;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stirante
 */
public abstract class AsyncTask<P, T, R> {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean done = new AtomicBoolean(false);

    @SafeVarargs
    public final void execute(P... params) {
        done.set(false);
        cancelled.set(false);
        new Thread(() -> {
            R result = doInBackground(params);
            done.set(true);
            if (!isCancelled()) {
                Platform.runLater(() -> onPostExecute(result));
            }
            if (isCancelled()) {
                Platform.runLater(this::onCancel);
            }
        }).start();
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

    public void onCancel() {

    }

    public boolean isDone() {
        return done.get();
    }

    public void cancel() {
        cancelled.set(true);
    }

}
