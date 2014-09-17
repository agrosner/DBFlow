package com.grosner.dbflow.runtime;

import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class manages a single table, wrapping all of the relevant
 * {@link com.grosner.dbflow.runtime.DatabaseManager} operations with the {@link ModelClass}
 */
public class TableManager<ModelClass extends Model> extends DatabaseManager {

    private Class<ModelClass> mTableClass;

    /**
     * Constructs a new instance. If createNewQueue is true, it will create a new looper. So only use this
     * if you need to have a second queue to have certain transactions go faster. If you create a new queue,
     * it will use up much more memory.
     * @param createNewQueue Create a separate request queue from the shared one.
     * @param mTableClass The table class this manager corresponds to
     */
    public TableManager(boolean createNewQueue, Class<ModelClass> mTableClass) {
        super(mTableClass.getSimpleName(), createNewQueue);
        this.mTableClass = mTableClass;
    }

    /**
     * Constructs a new instance.
     * @param mTableClass The table class this manager corresponds to
     */
    public TableManager(Class<ModelClass> mTableClass) {
        super(mTableClass.getSimpleName(), false);
        this.mTableClass = mTableClass;
    }

    public void fetchAllFromTable(ResultReceiver<List<ModelClass>> resultReceiver) {
        super.fetchAllFromTable(mTableClass, resultReceiver);
    }

    public void fetchFromTable(Select select, ResultReceiver<List<ModelClass>> resultReceiver) {
        super.fetchFromTable(mTableClass, select, resultReceiver);
    }

    public ModelClass selectModelWithWhere(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        return super.selectModelWithWhere(mTableClass, whereQueryBuilder);
    }

    public ModelClass selectModelById(Object... ids) {
        return super.selectModelById(mTableClass, ids);
    }

    public void selectModelWithWhere(ResultReceiver<ModelClass> resultReceiver, WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        super.selectModelWithWhere(mTableClass, resultReceiver, whereQueryBuilder);
    }

    public void selectModelById(ResultReceiver<ModelClass> resultReceiver, Object... ids) {
        super.selectModelById(mTableClass, resultReceiver, ids);
    }

    public void deleteTable(DBTransactionInfo transactionInfo) {
        super.deleteTable(transactionInfo, mTableClass);
    }

    public void deleteModelsWithQuery(DBTransactionInfo transactionInfo, WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        super.deleteModelsWithQuery(transactionInfo, mTableClass, whereQueryBuilder);
    }

}
