package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;

import java.util.List;

/**
 * Description: Provides a base implementation of {@link ModelQueriable} to simplify a lot of code.
 */
public abstract class BaseModelQueriable<ModelClass extends Model> implements ModelQueriable<ModelClass>, Query {

    private final Class<ModelClass> table;

    /**
     * Constructs new instance of this class and is meant for subclasses only.
     *
     * @param table the table that belongs to this query.
     */
    protected BaseModelQueriable(Class<ModelClass> table) {
        this.table = table;
    }

    @Override
    public List<ModelClass> queryList() {
        return SqlUtils.queryList(table, getQuery());
    }

    @Override
    public ModelClass querySingle() {
        return SqlUtils.querySingle(table, getQuery());
    }

    @Override
    public <ModelContainerClass extends ModelContainer<ModelClass, ?>> ModelContainerClass queryModelContainer(@NonNull ModelContainerClass instance) {
        return SqlUtils.convertToModelContainer(false, table, query(), instance);
    }

    @Override
    public Class<ModelClass> getTable() {
        return table;
    }

    @Override
    public FlowCursorList<ModelClass> queryCursorList() {
        return new FlowCursorList<>(false, this);
    }

    @Override
    public FlowQueryList<ModelClass> queryTableList() {
        return new FlowQueryList<>(this);
    }

    @Override
    public AsyncQuery<ModelClass> async() {
        return new AsyncQuery<>(this, TransactionManager.getInstance());
    }

    @Override
    public <QueryClass extends BaseQueryModel> List<QueryClass> queryCustomList(Class<QueryClass> queryModelClass) {
        return SqlUtils.queryList(queryModelClass, getQuery());
    }

    @Override
    public <QueryClass extends BaseQueryModel> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass) {
        return SqlUtils.querySingle(queryModelClass, getQuery());
    }

    @Override
    public void execute() {
        Cursor cursor = query();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
