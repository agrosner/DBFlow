package com.grosner.dbflow.runtime;

import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBManagerRuntime {


    private static ArrayList<TransactionManager> managers;

    static ArrayList<TransactionManager> getManagers() {
        if (managers == null) {
            managers = new ArrayList<TransactionManager>();
        }
        return managers;
    }

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

    public static void restartManagers() {
        for (TransactionManager manager : getManagers()) {
            manager.checkQueue();
        }
    }
}
