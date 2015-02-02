package com.raizlabs.android.dbflow.runtime;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.DeleteTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.InsertTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.UpdateTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.UpdateModelListTransaction;
import com.raizlabs.android.dbflow.sql.Queriable;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Collection;
import java.util.List;

/**
 * Description: This class manages batch database interactions. It is useful for retrieving, updating, saving,
 * and deleting lists of items. The bulk of DB operations should exist in this class.
 */
public class TransactionManager {

    /**
     * The shared database manager instance
     */
    private static TransactionManager manager;
    /**
     * Whether this manager has its own {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private final boolean hasOwnQueue;
    /**
     * Runs all of the UI threaded requests
     */
    protected Handler mRequestHandler = new Handler(Looper.getMainLooper());
    /**
     * The queue where we asynchronously perform database requests
     */
    private DBTransactionQueue mQueue;
    /**
     * The name of the associated {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    private String mName;

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

    void checkQueue() {
        if (!getQueue().isAlive()) {
            getQueue().start();
        }
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
        mQueue = null;
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
     * Runs UI operations in the handler with delay
     *
     * @param runnable
     */
    public synchronized void processOnRequestHandler(long delay, Runnable runnable) {
        mRequestHandler.postDelayed(runnable, delay);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param queriable       The {@link com.raizlabs.android.dbflow.sql.Queriable} statement that we wish to execute. The query base should not be a select as this
     *                        does not return any results.
     * @param <ModelClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void transactQuery(DBTransactionInfo transactionInfo, Queriable<ModelClass> queriable) {
        transactQuery(transactionInfo, queriable, null);
    }

    /**
     * Adds an arbitrary statement to be processed on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} in the background.
     *
     * @param transactionInfo           The information on how we should approach this request.
     * @param queriable                 The {@link com.raizlabs.android.dbflow.sql.Queriable} statement that we wish to execute.
     * @param cursorTransactionListener The cursor from the DB that we can process
     * @param <ModelClass>              The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void transactQuery(DBTransactionInfo transactionInfo, Queriable<ModelClass> queriable, TransactionListener<Cursor> cursorTransactionListener) {
        addTransaction(new QueryTransaction<ModelClass>(transactionInfo, queriable, cursorTransactionListener));
    }

    /**
     * Adds a transaction to the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transaction
     */
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    /**
     * Fetchs all items from the table in the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} with
     * the optional array of columns
     *
     * @param transactionListener        The result of the selection will be placed here on the main thread.
     * @param whereConditionQueryBuilder The where query conditions to use
     * @param columns                    The columns to select
     * @param <ModelClass>               The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public <ModelClass extends Model> void fetchFromTable(TransactionListener<List<ModelClass>> transactionListener,
                                                          ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder, String... columns) {
        addTransaction(new SelectListTransaction<ModelClass>(transactionListener, whereConditionQueryBuilder, columns));
    }

    // region Database Select Methods

    /**
     * Fetches all items from the table in the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} with the
     * optional array of {@link com.raizlabs.android.dbflow.sql.builder.Condition}.
     * This should be done for simulateneous requests on different threads.
     *
     * @param tableClass          The table we select from.
     * @param transactionListener The result of the selection will be placed here on the main thread.
     * @param conditions          The list of conditions to select the list of models from
     */
    public <ModelClass extends Model> void fetchFromTable(Class<ModelClass> tableClass,
                                                          TransactionListener<List<ModelClass>> transactionListener, Condition... conditions) {
        addTransaction(new SelectListTransaction<ModelClass>(transactionListener, tableClass, conditions));
    }

    /**
     * Fetches a list of {@link ModelClass} on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     *
     * @param conditionQueryBuilder The where query we will use
     * @param transactionListener   The result will be passed here.
     */
    public <ModelClass extends Model> void fetchFromTable(ConditionQueryBuilder<ModelClass> conditionQueryBuilder,
                                                          final TransactionListener<List<ModelClass>> transactionListener) {
        fetchFromTable(Where.with(conditionQueryBuilder), transactionListener);
    }

    /**
     * Fetches all items from the table with the specified {@link com.raizlabs.android.dbflow.sql.language.Where} in
     * the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
     *
     * @param where               The {@link com.raizlabs.android.dbflow.sql.language.Where} statement that we wish to execute. The base of this
     *                            query must be {@link com.raizlabs.android.dbflow.sql.language.Select}
     * @param transactionListener
     * @param <ModelClass>        The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchFromTable(Where<ModelClass> where, TransactionListener<List<ModelClass>> transactionListener) {
        addTransaction(new SelectListTransaction<ModelClass>(where, transactionListener));
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared. It reuses the cached {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     * if it exists for this table.
     *
     * @param tableClass          The table to select the model from.
     * @param transactionListener The result will be passed here.
     * @param ids                 The list of ids given by the {@link ModelClass}
     * @param <ModelClass>        The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchModelById(Class<ModelClass> tableClass,
                                                          final TransactionListener<ModelClass> transactionListener,
                                                          Object... ids) {
        ConditionQueryBuilder<ModelClass> queryBuilder = FlowManager.getPrimaryWhereQuery(tableClass);
        fetchModel(queryBuilder.replaceEmptyParams(ids), transactionListener);
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by the IDs passed in.
     * The order of the ids must match the ordered they're declared.
     *
     * @param conditionQueryBuilder The where query we will use
     * @param transactionListener   The result will be passed here.
     */
    public <ModelClass extends Model> void fetchModel(ConditionQueryBuilder<ModelClass> conditionQueryBuilder,
                                                      final TransactionListener<ModelClass> transactionListener) {
        fetchModel(Where.with(conditionQueryBuilder), transactionListener);
    }

    /**
     * Selects a single model on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} by
     * {@link com.raizlabs.android.dbflow.sql.language.From}.
     *
     * @param where               The where to use.
     * @param transactionListener The result will be passed here.
     * @param <ModelClass>        The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void fetchModel(Where<ModelClass> where,
                                                      final TransactionListener<ModelClass> transactionListener) {
        addTransaction(new SelectSingleModelTransaction<ModelClass>(where, transactionListener));
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

    /**
     * Saves the list of {@link ModelClass} into the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} will be called during this Transaction.
     *
     * @param modelInfo Holds information about this save request
     */
    public <ModelClass extends Model> void save(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new SaveModelTransaction<ModelClass>(modelInfo));
    }

    // endregion

    // region Database Delete methods

    /**
     * Deletes with the specified {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}.
     * The corresponding
     * {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} will be called during this Transaction.
     *
     * @param modelInfo Holds information about this delete request
     */
    public <ModelClass extends Model> void delete(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new DeleteModelListTransaction<ModelClass>(modelInfo));
    }

    /**
     * Deletes all of the models in the specified table with the list of {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     * on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param table           The table to delete models from.
     * @param conditions      The list of conditions to delete the list of models from
     * @param <ModelClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  Class<ModelClass> table, Condition... conditions) {
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, table, conditions));
    }

    /**
     * Deletes all of the models in the specified table with the {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     * on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo       The information on how we should approach this request.
     * @param conditionQueryBuilder The where arguments of the deletion
     * @param <ModelClass>          The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void delete(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        addTransaction(new DeleteTransaction<ModelClass>(transactionInfo, conditionQueryBuilder));
    }

    // endregion

    // region Database update methods

    /**
     * Updates the list of {@link ModelClass} into the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} will be called during this Transaction.
     *
     * @param modelInfo Holds information about this update request
     */
    public <ModelClass extends Model> void update(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new UpdateModelListTransaction<ModelClass>(modelInfo));
    }

    /**
     * Updates all of the models with the {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener}
     * will be called during this Transaction.
     *
     * @param transactionInfo       The information on how we should approach this request.
     * @param whereConditionBuilder The WHERE query piece
     * @param setConditions         The conditions for the SET part of the query
     * @param <ModelClass>          The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> whereConditionBuilder,
                                                  Condition... setConditions) {
        addTransaction(new UpdateTransaction<>(transactionInfo, whereConditionBuilder, setConditions));
    }

    /**
     * Updates a a table based on the WHERE condition builder, and SET condition builder.
     *
     * @param transactionInfo       The information on how we should approach this request.
     * @param whereConditionBuilder The WHERE condition
     * @param setConditionBuilder   The SET conditions
     * @param <ModelClass>          The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     * @see #update(DBTransactionInfo, com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder, com.raizlabs.android.dbflow.sql.builder.Condition...)
     */
    public <ModelClass extends Model> void update(DBTransactionInfo transactionInfo,
                                                  ConditionQueryBuilder<ModelClass> whereConditionBuilder,
                                                  ConditionQueryBuilder<ModelClass> setConditionBuilder) {
        addTransaction(new UpdateTransaction<>(transactionInfo, whereConditionBuilder, setConditionBuilder));
    }


    // endregion

    // region Database insert methods

    /**
     * Insers the list of {@link ModelClass} into the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     * with the specified {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}. The corresponding
     * {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} will be called during this Transaction.
     *
     * @param modelInfo Holds information about this update request
     */
    public <ModelClass extends Model> void insert(ProcessModelInfo<ModelClass> modelInfo) {
        addTransaction(new InsertModelTransaction<>(modelInfo));
    }

    /**
     * Updates all of the models with the {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}
     * passed from the list of models. The corresponding {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener}
     * will be called when the transaction finishes.
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param table           The table to insert into
     * @param columnValues    The conditions for columns and values to insert.
     * @param <ModelClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     */
    public <ModelClass extends Model> void insert(DBTransactionInfo transactionInfo,
                                                  Class<ModelClass> table,
                                                  Condition... columnValues) {
        addTransaction(new InsertTransaction<>(transactionInfo, table, columnValues));
    }

    /**
     * Runs the INSERT on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo The information on how we should approach this request.
     * @param insert          The INSERT statement to use.
     * @param <ModelClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     * @see #insert(DBTransactionInfo, Class, com.raizlabs.android.dbflow.sql.builder.Condition...)
     */
    public <ModelClass extends Model> void insert(DBTransactionInfo transactionInfo,
                                                  Insert<ModelClass> insert) {
        addTransaction(new InsertTransaction<>(transactionInfo, insert));
    }


    // endregion
}
