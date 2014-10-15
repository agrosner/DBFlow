package com.grosner.dbflow.runtime;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.runtime.transaction.DeleteTransaction;
import com.grosner.dbflow.runtime.transaction.QueryTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.runtime.transaction.SelectListTransaction;
import com.grosner.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.grosner.dbflow.runtime.transaction.UpdateTransaction;
import com.grosner.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.grosner.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.grosner.dbflow.runtime.transaction.process.UpdateModelListTransaction;
import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.sql.Queriable;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.sql.language.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
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
     * Creates the DatabaseManager while starting its own request queue
     *
     * @param name
     */
    public TransactionManager(String name, boolean createNewQueue) {
        mName = name;
        hasOwnQueue = createNewQueue;
        TransactionManagerRuntime.getManagers().add(this);
        checkQueue();
    }

    /**
     * Returns the application's only needed DBManager.
     * It uses the shared {@link com.grosner.dbflow.config.FlowManager}. If you wish to use a different DB from the norm,
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
    public static void transact(SQLiteDatabase database, Runnable runnable) {
        database.beginTransaction();
        try {
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable
     */
    public static void transact(Runnable runnable) {
        transact(FlowManager.getInstance().getWritableDatabase(), runnable);
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
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler(Looper.getMainLooper());

    /**
     * Runs UI operations in the handler
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(Runnable runnable) {
        mRequestHandler.post(runnable);
    }

    /**
     * Runs UI operations in the handler with delay
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(long delay, Runnable runnable) {
        mRequestHandler.postDelayed(runnable, delay);
    }

    /**
     * Adds a transaction to the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param queriable       The {@link com.grosner.dbflow.sql.Queriable} statement that we wish to execute. The query base should not be a select as this
     *                        does not return any results.
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void transactQuery(DBTransactionInfo transactionInfo, Queriable<ModelClass> queriable) {
        transactQuery(transactionInfo, queriable, null);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo      The information on how we should approach this request.
     * @param queriable            The {@link com.grosner.dbflow.sql.Queriable} statement that we wish to execute.
     * @param cursorResultReceiver The cursor from the DB that we can process
     * @param <ModelClass>         The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void transactQuery(DBTransactionInfo transactionInfo, Queriable<ModelClass> queriable, ResultReceiver<Cursor> cursorResultReceiver) {
        addTransaction(new QueryTransaction<ModelClass>(transactionInfo, queriable, cursorResultReceiver));
    }

    // region Database Select Methods

    /**
     * Fetchs all items from the table in the {@link com.grosner.dbflow.runtime.DBTransactionQueue} with
     * the optional array of columns
     *
     * @param resultReceiver             The result of the selection will be placed here on the main thread.
     * @param whereConditionQueryBuilder The where query conditions to use
     * @param columns                    The columns to select
     * @param <ModelClass>               The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void fetchFromTable(ResultReceiver<List<ModelClass>> resultReceiver,
                                                          ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder, String... columns) {
        addTransaction(new SelectListTransaction<ModelClass>(resultReceiver, whereConditionQueryBuilder, columns));
    }

    /**
     * Fetches all items from the table in the {@link com.grosner.dbflow.runtime.DBTransactionQueue} with the
     * optional array of {@link com.grosner.dbflow.sql.builder.Condition}.
     * This should be done for simulateneous requests on different threads.
     *
     * @param tableClass     The table we select from.
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     * @param conditions     The list of conditions to select the list of models from
     */
    public <ModelClass extends Model> void fetchFromTable(Class<ModelClass> tableClass,
                                                          ResultReceiver<List<ModelClass>> resultReceiver, Condition... conditions) {
        addTransaction(new SelectListTransaction<ModelClass>(resultReceiver, tableClass, conditions));
    }

    /**
     * Fetches all items from the table with the specified {@link com.grosner.dbflow.sql.language.Where} in
     * the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param where          The {@link com.grosner.dbflow.sql.language.Where} statement that we wish to execute. The base of this
     *                       query must be {@link com.grosner.dbflow.sql.language.Select}
     * @param resultReceiver
     * @param <ModelClass>   The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchFromTable(Where<ModelClass> where, ResultReceiver<List<ModelClass>> resultReceiver) {
        addTransaction(new SelectListTransaction<ModelClass>(where, resultReceiver));
    }

    /**
     * Fetches a list of {@link ModelClass} on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param conditionQueryBuilder The where query we will use
     * @param resultReceiver        The result will be passed here.
     */
    public <ModelClass extends Model> void fetchFromTable(ConditionQueryBuilder<ModelClass> conditionQueryBuilder,
                                                          final ResultReceiver<List<ModelClass>> resultReceiver) {
        fetchFromTable(Where.with(conditionQueryBuilder), resultReceiver);
    }

    /**
     * Selects a single model object with the specified {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The where query we will use
     * @param <ModelClass>          The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return the first model from the database cursor.
     */
    public <ModelClass extends Model> ModelClass selectModel(ConditionQueryBuilder<ModelClass> conditionQueryBuilder, String... columns) {
        return Where.with(conditionQueryBuilder, columns).querySingle();
    }

    /**
     * Selects all models from the table. (this method should be avoided as it could block the UI thread).
     *
     * @param tableClass   The table to select the list from
     * @param conditions   The list of conditions to select the list of models from
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the list of every row in the table
     */
    public <ModelClass extends Model> List<ModelClass> selectAllFromTable(Class<ModelClass> tableClass, Condition... conditions) {
        return new Select().from(tableClass).where(conditions).queryList();
    }

    /**
     * Selects a list of model objects with the specified {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The where query we will use
     * @param <ModelClass>          The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return the list of models from the database cursor.
     */
    public <ModelClass extends Model> List<ModelClass> selectAllFromTable(ConditionQueryBuilder<ModelClass> conditionQueryBuilder, String... columns) {
        return Where.with(conditionQueryBuilder, columns).queryList();
    }

    /**
     * Selects a single model with the specified ids on the same thread this is called. Looks up the cached
     * {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} for this {@link ModelClass} to reuse.
     *
     * @param tableClass   The table to select the model from
     * @param ids          The list of ids given by the {@link ModelClass}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @return
     */
    public <ModelClass extends Model> ModelClass selectModelById(Class<ModelClass> tableClass, Object... ids) {
        ConditionQueryBuilder<ModelClass> queryBuilder = FlowManager.getManagerForTable(tableClass).getStructure().getPrimaryWhereQuery(tableClass);
        return selectModel(queryBuilder.replaceEmptyParams(ids));
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by
     * {@link com.grosner.dbflow.sql.language.From}.
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
     * @param conditionQueryBuilder The where query we will use
     * @param resultReceiver        The result will be passed here.
     */
    public <ModelClass extends Model> void fetchModel(ConditionQueryBuilder<ModelClass> conditionQueryBuilder,
                                                      final ResultReceiver<ModelClass> resultReceiver) {
        fetchModel(Where.with(conditionQueryBuilder), resultReceiver);
    }

    /**
     * Selects a single model on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared. It reuses the cached {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
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
        ConditionQueryBuilder<ModelClass> queryBuilder = FlowManager.getManagerForTable(tableClass).getStructure().getPrimaryWhereQuery(tableClass);
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

        // Only start save queue if we are going to use it
        if (!getSaveQueue().isAlive()) {
            getSaveQueue().start();
        }
        getSaveQueue().addAll(models);
    }

    /**
     * Saves the list of {@link ModelClass} into the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.grosner.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.grosner.dbflow.runtime.transaction.ResultReceiver} will be called when the transaction completes.
     *
     * @param modelInfo Holds information about this save request
     */
    public <ModelClass extends Model> void save(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new SaveModelTransaction<ModelClass>(modelInfo));
    }

    // endregion

    // region Database Delete methods

    /**
     * Drops all of the rows from the specified table from the DB immediately.
     *
     * @param tableClass   The table to delete the models from.
     * @param conditions   The list of conditions to delete the list of models from
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void deleteTable(Class<ModelClass> tableClass, Condition... conditions) {
        Delete.table(tableClass, conditions);
    }

    /**
     * Deletes all of the models in the specified table on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param table           The table to delete models from.
     * @param conditions      The list of conditions to delete the list of models from
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void deleteTable(DBTransactionInfo transactionInfo,
                                                       Class<ModelClass> table, Condition... conditions) {
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, table, conditions));
    }

    /**
     * Deletes with the specified {@link com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo}.
     * The corresponding
     * {@link com.grosner.dbflow.runtime.transaction.ResultReceiver} will be called when the transaction completes.
     *
     * @param modelInfo Holds information about this delete request
     */
    public <ModelClass extends Model> void delete(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(modelInfo));
    }

    /**
     * Deletes all of the models in the specified table with the list of {@link com.grosner.dbflow.sql.builder.Condition}
     * on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param table           The table to delete models from.
     * @param conditions      The list of conditions to delete the list of models from
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  Class<ModelClass> table, Condition... conditions) {
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, table, conditions));
    }

    /**
     * Deletes all of the models in the specified table with the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
     * on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo       The information on how we should approach this request.
     * @param conditionQueryBuilder The where arguments of the deletion
     * @param <ModelClass>          The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, conditionQueryBuilder));
    }

    // endregion

    // region Database update methods

    /**
     * Updates the list of {@link ModelClass} into the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.grosner.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.grosner.dbflow.runtime.transaction.ResultReceiver} will be called when the transaction completes.
     *
     * @param modelInfo Holds information about this update request
     */
    public <ModelClass extends Model> void update(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(modelInfo));
    }

    /**
     * Updates all of the models with the {@link com.grosner.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
     * will be called when the transaction finishes.
     *
     * @param transactionInfo       The information on how we should approach this request.
     * @param whereConditionBuilder The whery query piece
     * @param setConditions         The conditions for the set part of the query
     * @param <ModelClass>          The class that implements {@link com.grosner.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> whereConditionBuilder,
                                                  Condition... setConditions) {
        addTransaction(new UpdateTransaction<ModelClass>(transactionInfo, whereConditionBuilder, setConditions));
    }

    /**
     * Used when we don't care about the result of {@link #delete(DBTransactionInfo, java.util.List, com.grosner.dbflow.runtime.transaction.ResultReceiver)}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param models          The list of models to update
     * @param <ModelClass>    The class that implements {@link com.grosner.dbflow.structure.Model}.
     * @see #update(DBTransactionInfo, com.grosner.dbflow.runtime.transaction.ResultReceiver, java.util.List)
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> whereConditionBuilder,
                                                  ConditionQueryBuilder<ModelClass> setConditionBuilder) {
        addTransaction(new UpdateTransaction<ModelClass>(transactionInfo, whereConditionBuilder, setConditionBuilder));
    }

    // endregion
}
