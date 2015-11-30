package com.raizlabs.android.dbflow.runtime;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;

/**
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. The bulk of DB operations should exist in this class.
 */
public class TransactionManager {

    /**
     * Runs all of the UI threaded requests
     */
    protected Handler requestHandler = new Handler(Looper.getMainLooper());

    /**
     * The shared database manager instance
     */
    private static TransactionManager manager;
    /**
     * Whether this manager has its own {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private final boolean hasOwnQueue;

    /**
     * The queue where we asynchronously perform database requests
     */
    private DBTransactionQueue transactionQueue;

    /**
     * The name of the associated {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private String name;

    /**
     * Creates the DatabaseManager while starting its own request queue
     *
     * @param name           The name to associate the running thread for transactions.
     * @param createNewQueue if true, we will create a new queue thread. Warning these can be expensive.
     */
    public TransactionManager(String name, boolean createNewQueue) {
        this.name = name;
        hasOwnQueue = createNewQueue;
        TransactionManagerRuntime.getManagers().add(this);
        checkQueue();
    }

    void checkQueue() {
        if (!getQueue().isAlive()) {
            getQueue().start();
        }
    }

    DBTransactionQueue getQueue() {
        if (transactionQueue == null) {
            if (hasOwnQueue) {
                transactionQueue = new DBTransactionQueue(name, this);
            } else {
                transactionQueue = TransactionManager.getInstance().transactionQueue;
            }
        }
        return transactionQueue;
    }

    /**
     * Returns the application's only needed DBManager.
     * It uses the shared {@link com.raizlabs.android.dbflow.config.FlowManager}. If you wish to use a different DB from the norm,
     * create a new instance of this class with a different manager.
     *
     * @return
     */
    public static TransactionManager getInstance() {
        if (manager == null) {
            manager = new TransactionManager(TransactionManager.class.getSimpleName(), true);
        }
        return manager;
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable
     */
    public static void transact(String databaseName, Runnable runnable) {
        transact(FlowManager.getDatabase(databaseName).getWritableDatabase(), runnable);
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable
     */
    public static void transact(SQLiteDatabase database, Runnable runnable) {
        database.beginTransaction();
        try {
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public boolean hasOwnQueue() {
        return hasOwnQueue;
    }

    /**
     * Destroys the running queue
     */
    void disposeQueue() {
        transactionQueue = null;
    }

    /**
     * Runs UI operations in the handler
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(Runnable runnable) {
        requestHandler.post(runnable);
    }

    /**
     * Runs UI operations in the handler with delay
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(long delay, Runnable runnable) {
        requestHandler.postDelayed(runnable, delay);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param queriable       The {@link Queriable} statement that we wish to execute. The query base should not be a select as this
     *                        does not return any results.
     */
    public void transactQuery(DBTransactionInfo transactionInfo, Queriable queriable) {
        transactQuery(transactionInfo, queriable, null);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo           The information on how we should approach this request.
     * @param queriable                 The {@link Queriable} statement that we wish to execute.
     * @param cursorTransactionListener The cursor from the DB that we can process
     */
    public void transactQuery(DBTransactionInfo transactionInfo, Queriable queriable,
                              TransactionListener<Cursor> cursorTransactionListener) {
        addTransaction(new QueryTransaction(transactionInfo, queriable, cursorTransactionListener));
    }

    /**
     * Adds a transaction to the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    public void cancelTransaction(BaseTransaction transaction) {
        getQueue().cancel(transaction);
    }

    /**
     * Saves the passed in model to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param model        The model to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(ModelClass model) {

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().add(model);
    }

    // endregion

    // region Database Save methods

    public DBBatchSaveQueue getSaveQueue() {
        return DBBatchSaveQueue.getSharedSaveQueue();
    }

    /**
     * Saves all of the passed in models to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param models       The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(Collection<ModelClass> models) {

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().addAll(models);
    }

    // endregion
}
