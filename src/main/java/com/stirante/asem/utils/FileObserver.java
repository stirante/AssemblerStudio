package com.stirante.asem.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by stirante
 */
public class FileObserver {

    private Map<WatchKey, WatchHolder> events = new HashMap<>();
    private AtomicBoolean running = new AtomicBoolean(true);
    private WatchService watcher;

    public FileObserver() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileObserver observer = new FileObserver();
        observer.registerListener(new File("bin").toPath(), System.out::println);
        observer.start();
    }

    public void registerListener(Path path, FileEventListener listener) {
        try {
            WatchKey register = path.register(watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
            WatchHolder holder = new WatchHolder();
            holder.listener = listener;
            holder.path = path;
            events.put(register, holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(() -> {
            while (running.get()) {
                try {
                    WatchKey take = watcher.take();
                    WatchHolder holder = events.get(take);
                    List<WatchEvent<?>> watchEvents = take.pollEvents();
                    for (WatchEvent<?> event : watchEvents) {
                        FileEvent e = new FileEvent(event, holder.path);
                        try {
                            holder.listener.onFileEvent(e);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    take.reset();
                } catch (InterruptedException ignored) {

                }
            }
        }).start();
    }

    public void stop() {
        running.set(false);
        try {
            watcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface FileEventListener {
        void onFileEvent(FileEvent event);
    }

    private class WatchHolder {
        private Path path;
        private FileEventListener listener;
    }

    public class FileEvent {
        private final WatchEvent<?> event;
        private final Path path;

        private FileEvent(WatchEvent<?> event, Path path) {
            this.event = event;
            this.path = path;
        }

        @SuppressWarnings("unchecked")
        public WatchEvent.Kind<Path> kind() {
            return (WatchEvent.Kind<Path>) event.kind();
        }

        public Path getPath() {
            return path.resolve((Path) event.context());
        }

        @Override
        public String toString() {
            return "FileEvent{" +
                    "kind=" + kind() +
                    ", path=" + getPath() +
                    '}';
        }
    }
}
