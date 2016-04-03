package com.raizlabs.android.dbflow.structure.database.transaction;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Description: Orders {@link Transaction} in a priority-based queue, enabling you perform higher priority
 * tasks first (such as retrievals) if you are attempting many DB operations at once.
 */
public class PriorityTransactionQueue extends Thread implements ITransactionQueue {

    private final PriorityBlockingQueue<PriorityEntry<Transaction>> queue;

    private boolean isQuitting = false;

    /**
     * Creates a queue with the specified name to ID it.
     *
     * @param name
     */
    public PriorityTransactionQueue(String name) {
        super(name);
        queue = new PriorityBlockingQueue<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Looper.prepare();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        PriorityEntry<Transaction> transaction;
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

            transaction.entry.executeSync();
        }
    }

    @Override
    public void add(Transaction transaction) {
        synchronized (queue) {
            PriorityEntry<Transaction> priorityEntry = new PriorityEntry<>(transaction);
            if (!queue.contains(priorityEntry)) {
                queue.add(priorityEntry);
            }
        }
    }

    /**
     * Cancels the specified request.
     *
     * @param transaction The transaction to cancel (if still in the queue).
     */
    @Override
    public void cancel(Transaction transaction) {
        synchronized (queue) {
            PriorityEntry<Transaction> priorityEntry = new PriorityEntry<>(transaction);
            if (queue.contains(priorityEntry)) {
                queue.remove(priorityEntry);
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
            Iterator<PriorityEntry<Transaction>> it = queue.iterator();
            while (it.hasNext()) {
                Transaction next = it.next().entry;
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
        isQuitting = true;
        interrupt();
    }

    private void throwInvalidTransactionType(Transaction transaction) {
        throw new IllegalArgumentException("Transaction of type:" +
                (transaction != null ? transaction.transaction().getClass() : "Unknown")
                + " should be of type PriorityTransactionWrapper");
    }

    class PriorityEntry<E extends Transaction>
            implements Comparable<PriorityEntry<Transaction>> {
        final E entry;
        final PriorityTransactionWrapper transactionWrapper;

        public PriorityEntry(E entry) {
            this.entry = entry;
            if (entry.transaction() instanceof PriorityTransactionWrapper) {
                transactionWrapper = ((PriorityTransactionWrapper) entry.transaction());
            } else {
                transactionWrapper = new PriorityTransactionWrapper.Builder(entry.transaction())
                        .build();
            }
        }

        public E getEntry() {
            return entry;
        }

        @Override
        public int compareTo(@NonNull PriorityEntry<Transaction> another) {
            return transactionWrapper.compareTo(another.transactionWrapper);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PriorityEntry<?> that = (PriorityEntry<?>) o;

            return transactionWrapper != null ? transactionWrapper.equals(that.transactionWrapper)
                    : that.transactionWrapper == null;

        }

        @Override
        public int hashCode() {
            return transactionWrapper != null ? transactionWrapper.hashCode() : 0;
        }
    }


}
