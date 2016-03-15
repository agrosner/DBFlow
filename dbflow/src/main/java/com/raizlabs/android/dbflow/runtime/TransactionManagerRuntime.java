package com.raizlabs.android.dbflow.runtime;

import java.util.ArrayList;

/**
 * Description: Holds onto {@link DefaultTransactionManager} when they are created.
 * Use this class to stop any running {@link DefaultTransactionQueue}
 */
public class TransactionManagerRuntime {

    private static ArrayList<BaseTransactionManager> managers;

    /**
     * Quits all active DBManager queues
     */
    public static void quit() {
        for (BaseTransactionManager manager : getManagers()) {
            manager.stopQueue();
        }
        DBBatchSaveQueue.getSharedSaveQueue().quit();
        DBBatchSaveQueue.disposeSharedQueue();
    }

    static ArrayList<BaseTransactionManager> getManagers() {
        if (managers == null) {
            managers = new ArrayList<>();
        }
        return managers;
    }

    /**
     * Will restart the {@link DefaultTransactionQueue} of every manager that
     * has its own queue.
     */
    public static void restartManagers() {
        for (BaseTransactionManager manager : getManagers()) {
            manager.checkQueue();
        }
    }
}
