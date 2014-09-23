package com.grosner.dbflow.runtime;

import android.os.Handler;
import android.os.Looper;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.runtime.transaction.DeleteTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.runtime.transaction.SelectListTransaction;
import com.grosner.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.grosner.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.grosner.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.grosner.dbflow.runtime.transaction.process.UpdateModelListTransaction;
import com.grosner.dbflow.sql.Delete;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.Collection;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. For the bulk of DB operations should exist in this class.
 */
public class TransactionManager {

    /**
     * The shared database manager instance
     */
    private static TransactionManager manager;

    /**
     * The queue where we asynchronously perform database requests
     */
    private DBTransactionQueue mQueue;

    /**
     * The name of the associated {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     */
    private String mName;

    /**
     * Whether this manager has its own {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     */
    private final boolean hasOwnQueue;

    /**
     * Provides full control over the DB
     */
    private FlowManager mManager;

    /**
     * Creates the DatabaseManager while starting its own request queue
     *
     * @param name
     */
    public TransactionManager(FlowManager flowManager, String name, boolean createNewQueue) {
        mManager = flowManager;
        mName = name;
        hasOwnQueue = createNewQueue;
        DBManagerRuntime.getManagers().add(this);
        checkQueue();
    }

    /**
     * Returns the application's only needed DBManager.
     * Note: this manager must be created on the main thread, otherwise a
     * {@link com.grosner.dbflow.runtime.DBManagerNotOnMainException} will be thrown. It uses the
     * shared {@link com.grosner.dbflow.config.FlowManager}. If you wish to use a different DB from the norm,
     * create a new instance of this class with the manager you want.
     *
     * @return
     */
    public static TransactionManager getInstance() {
        if (manager == null) {
            manager = new TransactionManager(FlowManager.getInstance(), TransactionManager.class.getSimpleName(), true);
        }
        return manager;
    }

    void checkQueue() {
        if (!getQueue().isAlive()) {
            getQueue().start();
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

    DBTransactionQueue getQueue() {
        if (mQueue == null) {
            if (hasOwnQueue) {
                mQueue = new DBTransactionQueue(mName);
            } else {
                mQueue = TransactionManager.getInstance().mQueue;
            }
        }
        return mQueue;
    }

    public DBBatchSaveQueue getSaveQueue() {
        return DBBatchSaveQueue.getSharedSaveQueue();
    }

    /**
     * Gets the {@link com.grosner.dbflow.config.FlowManager} that's responsible for the DB structure.
     *
     * @return
     */
    public FlowManager getFlowManager() {
        return mManager;
    }

    /**
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler(Looper.getMainLooper());

    /**
     * Runs a request from the DB in the request queue
     *
     * @param baseTransaction
     */
    protected void processOnBackground(BaseTransaction baseTransaction) {
        getQueue().add(baseTransaction);
    }

    /**
     * Runs UI operations in the handler
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(Runnable runnable) {
        mRequestHandler.post(runnable);
    }


    /**
     * Adds a transaction to the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    // region Database Select Methods

    /**
     * Selects all items from the table in the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     * This should be done for simulateneous requests on different threads.
     *
     * @param tableClass     The table we select from.
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     */
    public <ModelClass extends Model> void fetchAllFromTable(Class<ModelClass> tableClass,
                                                             ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new SelectListTransaction<ModelClass>(mManager, tableClass, resultReceiver));
    }

    /**
     * Selects all items from the table with the specified {@link com.grosner.dbflow.sql.From} in
     * the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param where          The {@link com.grosner.dbflow.sql.Where} statement that we wish to execute. The base of this
     *                       query must be {@link com.grosner.dbflow.sql.Select}
     * @param resultReceiver
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchFromTable(Where<ModelClass> where, ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new SelectListTransaction<ModelClass>(where, resultReceiver));
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared.
     *
     * @param whereQueryBuilder The where query we will use
     * @param resultReceiver    The result will be passed here.
     */
    public <ModelClass extends Model> void fetchFromTable(WhereQueryBuilder<ModelClass> whereQueryBuilder,
                                                          final ResultReceiver<List<ModelClass>> resultReceiver) {
        fetchFromTable(Where.with(mManager, whereQueryBuilder), resultReceiver);
    }

    /**
     * Selects a single model object with the specified {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder}
     *
     * @param whereQueryBuilder The where query we will use
     * @param <ModelClass>      The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return the first model from the database cursor.
     */
    public <ModelClass extends Model> ModelClass selectModel(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        return Where.with(mManager, whereQueryBuilder).querySingle();
    }

    /**
     * Selects all models from the table. (this method should be avoided as it could block the UI thread).
     *
     * @param tableClass   The table to select the list from
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the list of every row in the table
     */
    public <ModelClass extends Model> List<ModelClass> selectAllFromTable(Class<ModelClass> tableClass) {
        return new Select(mManager).from(tableClass).where().queryList();
    }

    /**
     * Selects a list of model objects with the specified {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder}
     *
     * @param whereQueryBuilder The where query we will use
     * @param <ModelClass>      The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return the list of models from the database cursor.
     */
    public <ModelClass extends Model> List<ModelClass> selectAllFromTable(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        return Where.with(mManager, whereQueryBuilder).queryList();
    }

    /**
     * Selects a single model with the specified ids on the same thread this is called. Looks up the cached
     * {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder} for this {@link ModelClass} to reuse.
     *
     * @param tableClass   The table to select the model from
     * @param ids          The list of ids given by the {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return
     */
    public <ModelClass extends Model> ModelClass selectModelById(Class<ModelClass> tableClass, Object... ids) {
        WhereQueryBuilder<ModelClass> queryBuilder = mManager.getStructure().getPrimaryWhereQuery(tableClass);
        return selectModel(queryBuilder.replaceEmptyParams(ids));
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by
     * {@link com.grosner.dbflow.sql.From}.
     *
     * @param where          The where to use.
     * @param resultReceiver The result will be passed here.
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchModel(Where<ModelClass> where,
                                                      final ResultReceiver<ModelClass> resultReceiver) {
        addTransaction(new SelectSingleModelTransaction<ModelClass>(where, resultReceiver));
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared.
     *
     * @param tableClass        The table to select the model from.
     * @param whereQueryBuilder The where query we will use
     * @param resultReceiver    The result will be passed here.
     */
    public <ModelClass extends Model> void fetchModel(WhereQueryBuilder<ModelClass> whereQueryBuilder,
                                                      final ResultReceiver<ModelClass> resultReceiver) {
        fetchModel(Where.with(mManager, whereQueryBuilder), resultReceiver);
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared. It reuses the {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder}
     * if it exists for this table.
     *
     * @param tableClass     The table to select the model from.
     * @param resultReceiver The result will be passed here.
     * @param ids            The list of ids given by the {@link ModelClass}
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchModelById(Class<ModelClass> tableClass,
                                                          final ResultReceiver<ModelClass> resultReceiver,
                                                          Object... ids) {
        WhereQueryBuilder<ModelClass> queryBuilder = mManager.getStructure().getPrimaryWhereQuery(tableClass);
        fetchModel(queryBuilder.replaceEmptyParams(ids), resultReceiver);
    }

    // endregion

    // region Database Save methods

    /**
     * Saves the passed in model to the {@link com.grosner.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param model        The model to save
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(ModelClass model) {

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().add(model);
    }

    /**
     * Saves all of the passed in models to the {@link com.grosner.dbflow.runtime.DBBatchSaveQueue}.
     * This method is recommended for saving large amounts of continuous data as to batch up as much data as possible in a save.
     *
     * @param models       The list of models to save
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void saveOnSaveQueue(Collection<ModelClass> models) {
        getSaveQueue().addAll(models);
    }

    /**
     * Saves the list of {@link ModelClass} into the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.grosner.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.grosner.dbflow.runtime.transaction.ResultReceiver} will be called when the transaction completes.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to save
     * @param resultReceiver  The models passed in here will be returned in this variable when the transaction completes.
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo,
                                                List<ModelClass> models, ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new SaveModelTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of this save()
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to save
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @see #save(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new SaveModelTransaction<ModelClass>(transactionInfo, null, models));
    }

    /**
     * Used when we don't care about the result of this save()
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param model           The single model to save
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @see #save(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)
     */
    public <ModelClass extends Model> void save(DBTransactionInfo transactionInfo, ModelClass model) {
        addTransaction(new SaveModelTransaction<ModelClass>(transactionInfo, null, model));
    }

    // endregion

    // region Database Delete methods

    /**
     * Drop the specified table from the DB.
     *
     * @param tableClass   The table to delete the models from.
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void dropTable(Class<ModelClass> tableClass) {
        new Delete().from(tableClass).where().query();
    }

    /**
     * Deletes all of the models in the specified table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param table           The table to delete models from.
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void deleteTable(DBTransactionInfo transactionInfo, Class<ModelClass> table) {
        addTransaction(new DeleteTransaction<ModelClass>(mManager, transactionInfo, table));
    }

    /**
     * Deletes all of the models in the specified table with the {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder}
     * on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo   The information on how we should approach this request.
     * @param whereQueryBuilder The where arguments of the deletion
     * @param table             The table to delete models from.
     * @param <ModelClass>      The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        addTransaction(new DeleteTransaction<ModelClass>(mManager, transactionInfo, whereQueryBuilder));
    }

    /**
     * Deletes all of the models with the {@link com.grosner.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
     * will be called when the transaction finishes.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to delete
     * @param resultReceiver  The models passed in here will be returned in this variable when the transaction completes.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  List<ModelClass> models, ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of {@link #delete(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to delete
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @see #delete(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(transactionInfo, null, models));
    }

    // endregion


    // region Database update methods

    /**
     * Updates all of the models with the {@link com.grosner.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
     * will be called when the transaction finishes.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param resultReceiver  The models passed in here will be returned in this variable when the transaction completes.
     * @param models          The list of models to update
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ResultReceiver<List<ModelClass>> resultReceiver, List<ModelClass> models) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(transactionInfo, resultReceiver, models));
    }

    /**
     * Used when we don't care about the result of {@link #delete(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to update
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @see #update(DBTransactionInfo, com.grosner.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo, List<ModelClass> models) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(transactionInfo, null, models));
    }

    // endregion
}
