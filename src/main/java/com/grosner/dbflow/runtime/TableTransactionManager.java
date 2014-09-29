package com.grosner.dbflow.runtime;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class manages a single table, wrapping all of the relevant
 * {@link TransactionManager} operations with the {@link ModelClass}
 */
public class TableTransactionManager<ModelClass extends Model> extends TransactionManager {

    private Class<ModelClass> mTableClass;

    /**
     * Constructs a new instance. If createNewQueue is true, it will create a new looper. So only use this
     * if you need to have a second queue to have certain transactions go faster. If you create a new queue,
     * it will use up much more memory.
     *
     * @param flowManager    The manager of the whole DB structure
     * @param createNewQueue Create a separate request queue from the shared one.
     * @param mTableClass    The table class this manager corresponds to
     */
    public TableTransactionManager(FlowManager flowManager, boolean createNewQueue, Class<ModelClass> mTableClass) {
        super(flowManager, mTableClass.getSimpleName(), createNewQueue);
        this.mTableClass = mTableClass;
    }

    /**
     * Constructs a new instance of this class with the shared {@link com.grosner.dbflow.config.FlowManager} and
     * uses the shared {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param mTableClass The table class this manager corresponds to
     */
    public TableTransactionManager(Class<ModelClass> mTableClass) {
        super(FlowManager.getInstance(), mTableClass.getSimpleName(), false);
        this.mTableClass = mTableClass;
    }

    /**
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     * @see #fetchFromTable(Class, com.grosner.dbflow.runtime.transaction.ResultReceiver, com.grosner.dbflow.sql.builder.Condition...)
     */
    public void fetchFromTable(ResultReceiver<List<ModelClass>> resultReceiver, Condition... conditions) {
        super.fetchFromTable(mTableClass, resultReceiver, conditions);
    }

    /**
     * @return
     * @see #selectAllFromTable(Class)
     */
    public List<ModelClass> selectAllFromTable() {
        return selectAllFromTable(mTableClass);
    }

    /**
     * @param ids The list of ids given by the {@link ModelClass}
     * @return
     * @see #selectModelById(Class, Object...)
     */
    public ModelClass selectModelById(Object... ids) {
        return super.selectModelById(mTableClass, ids);
    }

    /**
     * @param resultReceiver The result will be passed here.
     * @param ids            The list of ids given by the {@link ModelClass}
     * @see #fetchModelById(Class, com.grosner.dbflow.runtime.transaction.ResultReceiver, Object...)
     */
    public void fetchModelById(ResultReceiver<ModelClass> resultReceiver, Object... ids) {
        super.fetchModelById(mTableClass, resultReceiver, ids);
    }

    /**
     * @see #deleteTable(Class)
     */
    public void deleteTable() {
        super.deleteTable(mTableClass);
    }

    /**
     * @param transactionInfo The information on how we should approach this request.
     * @see #deleteTable(DBTransactionInfo, Class)
     */
    public void deleteTable(DBTransactionInfo transactionInfo) {
        super.deleteTable(transactionInfo, mTableClass);
    }

    /**
     * Returns the table class for this Table Transaction manager
     *
     * @return
     */
    public Class<ModelClass> getTableClass() {
        return mTableClass;
    }
}
