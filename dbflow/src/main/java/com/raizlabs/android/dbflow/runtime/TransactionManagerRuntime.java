package com.raizlabs.android.dbflow.runtime;

import java.util.ArrayList;

/**
 * Description: Holds onto {@link com.raizlabs.android.dbflow.runtime.TransactionManager} when they are created.
 * Use this class to stop any running {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class TransactionManagerRuntime {

    private static ArrayList<TransactionManager> managers;

    /**
     * Quits all active DBManager queues
     */
    public static void quit() {
        for (TransactionManager manager : getManagers()) {
            if (manager.hasOwnQueue()) {
                manager.getQueue().quit();
                manager.disposeQueue();
            }
        }
        DBBatchSaveQueue.getSharedSaveQueue().quit();
        DBBatchSaveQueue.disposeSharedQueue();
    }

    static ArrayList<TransactionManager> getManagers() {
        if (managers == null) {
            managers = new ArrayList<>();
        }
        return managers;
    }

    /**
     * Will restart the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} of every manager that
     * has its own queue.
     */
    public static void restartManagers() {
        for (TransactionManager manager : getManagers()) {
            if(manager.hasOwnQueue()) {
                manager.checkQueue();
            }
        }
    }
}
