package com.raizlabs.android.dbflow.structure.database.transaction;

import android.os.Looper;
import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description: Handles concurrent requests to the database and puts them in FIFO order based on a
 * {@link LinkedBlockingQueue}. As requests come in, they're placed in order and ran one at a time
 * until the queue becomes empty.
 */
public class DefaultTransactionQueue extends Thread implements ITransactionQueue {

    private final LinkedBlockingQueue<Transaction> queue;

    private boolean isQuitting = false;

    /**
     * Creates a queue with the specified name to ID it.
     *
     * @param name
     */
    public DefaultTransactionQueue(String name) {
        super(name);
        queue = new LinkedBlockingQueue<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        Transaction transaction;
        while (true) {
            try {
                transaction = queue.take();
            } catch (InterruptedException e) {
                synchronized (this) {
                    if (isQuitting) {
                        synchronized (queue) {
                            queue.clear();
                        }
                        return;
                    }
                }
                continue;
            }

            if (!isQuitting) {
                transaction.executeSync();
            }
        }
    }

    @Override
    public void add(@NonNull Transaction runnable) {
        synchronized (queue) {
            if (!queue.contains(runnable)) {
                queue.add(runnable);
            }
        }
    }

    /**
     * Cancels the specified request.
     *
     * @param runnable
     */
    @Override
    public void cancel(@NonNull Transaction runnable) {
        synchronized (queue) {
            if (queue.contains(runnable)) {
                queue.remove(runnable);
            }
        }
    }

    /**
     * Cancels all requests by a specific tag
     *
     * @param tag
     */
    @Override
    public void cancel(@NonNull String tag) {
        synchronized (queue) {
            Iterator<Transaction> it = queue.iterator();
            while (it.hasNext()) {
                Transaction next = it.next();
                if (next.name() != null && next.name().equals(tag)) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void startIfNotAlive() {
        synchronized (this) {
            if (!isAlive()) {
                try {
                    start();
                } catch (IllegalThreadStateException i) {
                    // log if failure from thread is still alive.
                    FlowLog.log(FlowLog.Level.E, i);
                }
            }
        }
    }

    /**
     * Quits this process
     */
    @Override
    public void quit() {
        synchronized (this) {
            isQuitting = true;
        }
        interrupt();
    }
}

