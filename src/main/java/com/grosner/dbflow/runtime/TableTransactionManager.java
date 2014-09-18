package com.grosner.dbflow.runtime;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
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
     * @see #fetchAllFromTable(Class, com.grosner.dbflow.runtime.transaction.ResultReceiver)
     */
    public void fetchAllFromTable(ResultReceiver<List<ModelClass>> resultReceiver) {
        super.fetchAllFromTable(mTableClass, resultReceiver);
    }

    /**
     * @param select         The select statement to run
     * @param resultReceiver The result of the selection will be placed here on the main thread.
     * @see #fetchFromTable(com.grosner.dbflow.sql.Select, com.grosner.dbflow.runtime.transaction.ResultReceiver)
     */
    public void fetchFromTable(Select select, ResultReceiver<List<ModelClass>> resultReceiver) {
        super.fetchFromTable(mTableClass, select, resultReceiver);
    }

    /**
     * @param whereQueryBuilder The where query we will use
     * @return the first model from the database cursor.
     * @see #selectModelWithWhere(com.grosner.dbflow.sql.builder.WhereQueryBuilder)
     */
    public ModelClass selectModelWithWhere(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        return super.selectModelWithWhere(mTableClass, whereQueryBuilder);
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
     * @param whereQueryBuilder The where query we will use
     * @param resultReceiver    The result will be passed here.
     * @see #selectModelWithWhere(com.grosner.dbflow.runtime.transaction.ResultReceiver, com.grosner.dbflow.sql.builder.WhereQueryBuilder)
     */
    public void selectModelWithWhere(ResultReceiver<ModelClass> resultReceiver, WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        super.fetchModelWithWhere(mTableClass, whereQueryBuilder, resultReceiver);
    }

    /**
     * @param resultReceiver The result will be passed here.
     * @param ids            The list of ids given by the {@link ModelClass}
     * @see #selectModelById(Class, com.grosner.dbflow.runtime.transaction.ResultReceiver, Object...)
     */
    public void selectModelById(ResultReceiver<ModelClass> resultReceiver, Object... ids) {
        super.selectModelById(mTableClass, resultReceiver, ids);
    }

    /**
     * @param transactionInfo The information on how we should approach this request.
     * @see #deleteTable(DBTransactionInfo, Class)
     */
    public void deleteTable(DBTransactionInfo transactionInfo) {
        super.deleteTable(transactionInfo, mTableClass);
    }

    /**
     * @param transactionInfo   The information on how we should approach this request.
     * @param whereQueryBuilder The where query we will use
     * @see #deleteModelsWithQuery(DBTransactionInfo, Class, com.grosner.dbflow.sql.builder.WhereQueryBuilder)
     */
    public void deleteModelsWithQuery(DBTransactionInfo transactionInfo, WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        super.deleteModelsWithQuery(transactionInfo, mTableClass, whereQueryBuilder);
    }

}
