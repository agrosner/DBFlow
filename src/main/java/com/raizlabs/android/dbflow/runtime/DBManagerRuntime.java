package com.raizlabs.android.dbflow.runtime;

import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBManagerRuntime {


    private static ArrayList<DatabaseManager> managers;

    static ArrayList<DatabaseManager> getManagers(){
        if(managers==null){
            managers = new ArrayList<DatabaseManager>();
        }
        return managers;
    }

    /**
     * Quits all active DBManager queues
     */
    public static void quit(){
        for(DatabaseManager manager: getManagers()){
            if(manager.hasOwnQueue()) {
                manager.getQueue().quit();
                manager.disposeQueue();
            }
        }
        DBBatchSaveQueue.getSharedSaveQueue().quit();
        DBBatchSaveQueue.disposeSharedQueue();
    }

    public static void restartManagers(){
        for(DatabaseManager manager: getManagers()){
            manager.checkQueue();
        }
    }
}
