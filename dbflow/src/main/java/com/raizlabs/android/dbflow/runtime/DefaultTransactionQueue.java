package com.raizlabs.android.dbflow.runtime;

import android.os.Handler;
import android.os.Looper;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Description: will handle concurrent requests to the DB based on priority
 */
public class DefaultTransactionQueue extends Thread implements ITransactionQueue {

    private final PriorityBlockingQueue<BaseTransaction> queue;

    private boolean isQuitting = false;

    /**
     * Runs all of the transactions {@link BaseTransaction#onPostExecute(Object)} on main thread.
     */
    private Handler requestHandler = new Handler(Looper.getMainLooper());

    /**
     * Creates a queue with the specified name to ID it.
     *
     * @param name
     */
    public DefaultTransactionQueue(String name) {
        super(name);
        queue = new PriorityBlockingQueue<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        BaseTransaction transaction;
        while (true) {
            try {
                synchronized (queue) {
                    transaction = queue.take();
                }
            } catch (InterruptedException e) {
                if (isQuitting) {
                    synchronized (queue) {
                        queue.clear();
                    }
                    return;
                }
                continue;
            }

            try {
                // If the transaction is ready
                if (transaction.onReady()) {

                    // Retrieve the result of the transaction
                    final Object result = transaction.onExecute();
                    final BaseTransaction finalTransaction = transaction;

                    // Run the result on the FG
                    if (transaction.hasResult(result)) {
                        requestHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finalTransaction.onPostExecute(result);
                            }
                        });
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

    }

    @Override
    public void add(BaseTransaction runnable) {
        if (!queue.contains(runnable)) {
            queue.add(runnable);
        }
    }

    /**
     * Cancels the specified request.
     *
     * @param runnable
     */
    @Override
    public void cancel(BaseTransaction runnable) {
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
    public void cancel(String tag) {
        synchronized (queue) {
            Iterator<BaseTransaction> it = queue.iterator();
            while (it.hasNext()) {
                BaseTransaction next = it.next();
                if (next.getName().equals(tag)) {
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
        isQuitting = true;
        interrupt();
    }
}

