package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

/**
 * Description: Provides a base implementation of {@link ModelQueriable} to simplify a lot of code.
 */
public abstract class BaseModelQueriable<ModelClass extends Model> implements ModelQueriable<ModelClass>, Query {

    private final Class<ModelClass> table;
    private final InstanceAdapter<?, ModelClass> retrievalAdapter;

    /**
     * Constructs new instance of this class and is meant for subclasses only.
     *
     * @param table the table that belongs to this query.
     */
    protected BaseModelQueriable(Class<ModelClass> table) {
        this.table = table;
        //noinspection unchecked
        retrievalAdapter = FlowManager.getInstanceAdapter(table);
    }

    @Override
    public List<ModelClass> queryList() {
        return retrievalAdapter.getListModelLoader().load(getQuery());
    }

    @Override
    public ModelClass querySingle() {
        return retrievalAdapter.getSingleModelLoader().load(getQuery());
    }

    @Override
    public ModelClass querySingle(DatabaseWrapper wrapper) {
        return retrievalAdapter.getSingleModelLoader().load(wrapper, getQuery());
    }

    @Override
    public List<ModelClass> queryList(DatabaseWrapper wrapper) {
        return retrievalAdapter.getListModelLoader().load(wrapper, getQuery());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <ModelContainerClass extends ModelContainer<ModelClass, ?>> ModelContainerClass queryModelContainer(@NonNull ModelContainerClass instance) {
        return (ModelContainerClass) FlowManager.getContainerAdapter(table).getModelContainerLoader().load(getQuery(), instance);
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
        return FlowManager.getQueryModelAdapter(queryModelClass).getListModelLoader().load(getQuery());
    }

    @Override
    public <QueryClass extends BaseQueryModel> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass) {
        return FlowManager.getQueryModelAdapter(queryModelClass).getSingleModelLoader().load(getQuery());
    }

    @Override
    public void execute(DatabaseWrapper wrapper) {
        Cursor cursor = query(wrapper);
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void execute() {
        Cursor cursor = query();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public DatabaseStatement compileStatement() {
        return FlowManager.getDatabaseForTable(table).getWritableDatabase().compileStatement(getQuery());
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
