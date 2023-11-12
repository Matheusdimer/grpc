package com.unesc.leilao.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskUtil {

    public interface Task {
        void cancel();
    }

    public static Task futureTask(Runnable runnable, long minutes) {
        AtomicBoolean canceled = new AtomicBoolean(false);
        CompletableFuture.runAsync(() -> {
            long start = System.currentTimeMillis();
            long end = start + minutes * 1000 * 60;

            while (System.currentTimeMillis() < end && !canceled.get()) {
            }

            if (!canceled.get()) {
                runnable.run();
            }
        });
        return () -> canceled.set(true);
    }

}
