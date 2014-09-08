package com.raizlabs.android.dbflow.runtime;

import android.os.Handler;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.DeleteModelListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.DeleteTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.FetchTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.runtime.transaction.SaveTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.UpdateModelListTransaction;
import com.raizlabs.android.dbflow.sql.From;
import com.raizlabs.android.dbflow.sql.Select;
import com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. For the bulk of DB operations should exist in this class.
 */
public class DatabaseManager {

    /**
     * The shared database manager instance
     */
    private static DatabaseManager manager;

    /**
     * The queue where we asynchronously perform database requests
     */
    private DBTransactionQueue mQueue;

    /**
     * The name of the associated {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private String mName;

    /**
     * Whether this manager has its own {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private final boolean hasOwnQueue;

    /**
     * Creates the DatabaseManager while starting its own request queue
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

    DBTransactionQueue getQueue(){
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


    /**
     * Adds a transaction to the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    // region Database Select Methods

    /**
     * Selects all items from the table in the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * This should be done for simulateneous requests on different threads.
     * @param tableClass The table we select from.
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     */
    public <ModelClass extends Model> void selectAllFromTable(Class<ModelClass> tableClass,
                                                                   ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new FetchTransaction<ModelClass>(tableClass, resultReceiver));
    }

    /**
     * Selects all items from the table with the specified {@link com.raizlabs.android.dbflow.sql.Select} in
     * the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * @param tableClass The table we select from.
     * @param select Provides the columns that we wish to select
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void selectFromTable(Class<ModelClass> tableClass, Select select,
                                                                   ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new FetchTransaction<ModelClass>(tableClass, select, resultReceiver));
    }

    /**
     * Selects all items from the table with the specified {@link com.raizlabs.android.dbflow.sql.From} in
     * the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * @param from The {@link com.raizlabs.android.dbflow.sql.From} statement that we wish to execute. The base of this
     *             query must be {@link com.raizlabs.android.dbflow.sql.Select}
     * @param resultReceiver
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void selectFromTable(From<ModelClass> from, ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new FetchTransaction<ModelClass>(from, resultReceiver));
    }

    public <ModelClass extends Model> ModelClass selectModelWithWhere(Class<ModelClass> tableClass,
                                                                      AbstractWhereQueryBuilder<ModelClass> whereQueryBuilder,
                                                                      String...values) {
        return new Select().from(tableClass).where(whereQueryBuilder, values).querySingle();
    }

    /**
     * Selects a single model on the same thread this is called. It is not recommended when there is heavy traffic
     * on the DB and this runs on the main thread.
     * @param tableClass
     * @param ids
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     * @return
     */
    public <ModelClass extends Model> ModelClass selectModelById(Class<ModelClass> tableClass, String...ids) {
        AbstractWhereQueryBuilder<ModelClass> queryBuilder = FlowManager.getCache().getStructure().getPrimaryWhereQuery(tableClass);
        return selectModelWithWhere(tableClass, queryBuilder, ids);
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by
     * {@link com.raizlabs.android.dbflow.sql.From}.
     * @param from The from to use.
     * @param resultReceiver The result will be passed here.
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void selectSingleModel(From<ModelClass> from,
                                                             final ResultReceiver<ModelClass> resultReceiver) {
        addTransaction(new FetchTransaction<ModelClass>(from, new ResultReceiver<List<ModelClass>>() {
            @Override
            public void onResultReceived(List<ModelClass> modelClasses) {
                if(!modelClasses.isEmpty()) {
                    resultReceiver.onResultReceived(modelClasses.get(0));
                }
            }
        }));
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared.
     * @param tableClass The table to select the model from.
     * @param resultReceiver The result will be passed here.
     * @param ids The list of ids given by the {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void selectModelWithWhere(Class<ModelClass> tableClass,
                                                                final ResultReceiver<ModelClass> resultReceiver,
                                                                AbstractWhereQueryBuilder<ModelClass> whereQueryBuilder,
                                                                String... ids) {
        From<ModelClass> modelFrom = new Select().from(tableClass).where(whereQueryBuilder, ids);
        selectSingleModel(modelFrom, resultReceiver);
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared.
     * @param tableClass The table to select the model from.
     * @param resultReceiver The result will be passed here.
     * @param ids The list of ids given by the {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void selectModelById(Class<ModelClass> tableClass,
                                                            final ResultReceiver<ModelClass> resultReceiver,
                                                            String...ids) {
        AbstractWhereQueryBuilder<ModelClass> queryBuilder = FlowManager.getCache().getStructure().getPrimaryWhereQuery(tableClass);
        selectModelWithWhere(tableClass, resultReceiver, queryBuilder, ids);
    }

    // endregion

    // region Database Save methods

    /**
     * Saves the passed in model to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     * @param model The model to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(ModelClass model) {
        getSaveQueue().add(model);
    }

    /**
     * Saves all of the passed in models to the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}.
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
        addTransaction(new SaveTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of this save()
     * @param transactionInfo The information on how we should approach this request.
     * @param models The list of models to save
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     *
     * @see #save(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new SaveTransaction<ModelClass>(transactionInfo, null, models));
    }

    // endregion

    // region Database Delete methods

    /**
     * Deletes all of the models in the specified table on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     * @param transactionInfo The information on how we should approach this request.
     * @param table The table to delete models from.
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void deleteTable(DBTransactionInfo transactionInfo, Class<ModelClass> table){
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, table));
    }

    /**
     * Deletes all of the models in the specified table with the {@link com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder}
     * on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * @param transctionInfo The information on how we should approach this request.
     * @param whereQueryBuilder The where arguments of the deletion
     * @param table The table to delete models from.
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void deleteModelsWithQuery(DBTransactionInfo transctionInfo,
                                                                 AbstractWhereQueryBuilder<ModelClass> whereQueryBuilder,
                                                                 Class<ModelClass> table) {
        addTransaction(new DeleteTransaction<ModelClass>(transctionInfo, whereQueryBuilder, table));
    }

    /**
     * Deletes all of the models with the {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver}
     * will be called when the transaction finishes.
     * @param transactionInfo The information on how we should approach this request.
     * @param resultReceiver The models passed in here will be returned in this variable when the transaction completes.
     * @param models The list of models to delete
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  ResultReceiver<List<ModelClass>> resultReceiver, List<ModelClass> models) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of {@link #delete(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)}
     * @param transactionInfo The information on how we should approach this request.
     * @param models The list of models to delete
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     *
     * @see #delete(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(transactionInfo, null, models));
    }

    // endregion


    // region Database update methods

    /**
     * Updates all of the models with the {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver}
     * will be called when the transaction finishes.
     * @param transactionInfo The information on how we should approach this request.
     * @param resultReceiver The models passed in here will be returned in this variable when the transaction completes.
     * @param models The list of models to update
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ResultReceiver<List<ModelClass>> resultReceiver, List<ModelClass> models) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of {@link #delete(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)}
     * @param transactionInfo The information on how we should approach this request.
     * @param models The list of models to update
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     *
     * @see #update(DBTransactionInfo, com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(transactionInfo, null, models));
    }

    // endregion
}
