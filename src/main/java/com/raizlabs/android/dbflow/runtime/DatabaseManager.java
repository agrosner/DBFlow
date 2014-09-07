package com.raizlabs.android.dbflow.runtime;

import android.os.Handler;

import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.FetchTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.runtime.transaction.SaveTransaction;
import com.raizlabs.android.dbflow.sql.From;
import com.raizlabs.android.dbflow.sql.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DatabaseManager {
    private static DatabaseManager manager;

    private DBTransactionQueue mQueue;

    private String mName;

    private  final boolean hasOwnQueue;

    /**
     * Creates the SingleDBManager while starting its own request queue
     * @param name
     */
    public DatabaseManager(String name, boolean createNewQueue){
        mName = name;
        hasOwnQueue = createNewQueue;
        checkThread();
        DBManagerRuntime.getManagers().add(this);
        checkQueue();
    }

    /**
     * Returns the application's only needed DBManager.
     * Note: this manager must be created on the main thread, otherwise a
     * {@link com.raizlabs.android.dbflow.runtime.DBManagerNotOnMainException} will be thrown
     * @return
     */
    public static DatabaseManager getSharedInstance(){
        if(manager==null){
            manager = new DatabaseManager("SingleDBManager", true);
        }
        return manager;
    }

    void checkQueue() {
        if (!getQueue().isAlive()) {
            getQueue().start();
        }
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
    }

    public boolean hasOwnQueue() {
        return hasOwnQueue;
    }

    /**
     * Destroys the running queue
     */
    void disposeQueue() {
        mQueue = null;
    }

    public DBTransactionQueue getQueue(){
        if(mQueue==null){
            if(hasOwnQueue) {
                mQueue = new DBTransactionQueue(mName);
            } else{
                mQueue = DatabaseManager.getSharedInstance().mQueue;
            }
        }
        return mQueue;
    }

    public DBBatchSaveQueue getSaveQueue(){
        return DBBatchSaveQueue.getSharedSaveQueue();
    }

    /**
     * Ensure manager was created in the main thread, otherwise handler will not work
     */
    protected void checkThread(){
        if(!Thread.currentThread().getName().equals("main")){
            throw new DBManagerNotOnMainException("DBManager needs to be instantiated on the main thread so Handler is on UI thread. Was on : " + Thread.currentThread().getName());
        }
    }

    /**
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler();

    /**
     * Runs a request from the DB in the request queue
     * @param runnable
     */
    protected void processOnBackground(BaseTransaction runnable){
        getQueue().add(runnable);
    }

    /**
     * Runs UI operations in the handler
     * @param runnable
     */
    protected synchronized void processOnForeground(Runnable runnable){
        mRequestHandler.post(runnable);
    }

    // region Database Getter Methods

    /**
     * Adds a transaction to the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    /**
     * Selects all items from the table in the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * This should be done for simulateneous requests on different threads.
     * @param tableClass
     * @param transaction
     */
    public <ModelClass extends Model> void selectAllFromTable(Class<ModelClass> tableClass,
                                                                   ResultReceiver<List<ModelClass>> resultReceiver) {
        getQueue().add(new FetchTransaction<ModelClass>(tableClass, resultReceiver));
    }

    /**
     * Selects all items from the table with the specified {@link com.raizlabs.android.dbflow.sql.Select} in
     * the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * @param tableClass
     * @param select
     * @param resultReceiver
     * @param <ModelClass>
     */
    public <ModelClass extends Model> void selectFromTable(Class<ModelClass> tableClass, Select select,
                                                                   ResultReceiver<List<ModelClass>> resultReceiver) {
        getQueue().add(new FetchTransaction<ModelClass>(tableClass, select, resultReceiver));
    }

    /**
     * Selects all items from the table with the specified {@link com.raizlabs.android.dbflow.sql.From} in
     * the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * @param from
     * @param resultReceiver
     * @param <ModelClass>
     */
    public <ModelClass extends Model> void selectFromTable(From from, ResultReceiver<List<ModelClass>> resultReceiver) {
        getQueue().add(new FetchTransaction<ModelClass>(from, resultReceiver));
    }

    /**
     * Saves al of the passed in models to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     * @param models The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(Collection<ModelClass> models) {
        getSaveQueue().addAll(models);
    }

    /**
     * Saves the list of {@link ModelClass} into the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver} will be called when the transaction completes.
     * @param transactionInfo The information on how we should approach this request.
     * @param resultReceiver The models passed in here will be returned in this variable when the transaction completes.
     * @param models The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo,
                                                ResultReceiver<List<ModelClass>> resultReceiver, List<ModelClass> models) {
        getQueue().add(new SaveTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of this save()
     * @param transactionInfo The information on how we should approach this request.
     * @param models The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     *
     *     @see #save(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        getQueue().add(new SaveTransaction<ModelClass>(transactionInfo, null, models));
    }


    // endregion
}
