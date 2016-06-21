package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides a base implementation of {@link ModelQueriable} to simplify a lot of code. It provides the
 * default implementation for convenience.
 */
public abstract class BaseModelQueriable<TModel extends Model> extends BaseQueriable<TModel> implements ModelQueriable<TModel>, Query {

    private final InstanceAdapter<?, TModel> retrievalAdapter;

    /**
     * Constructs new instance of this class and is meant for subclasses only.
     *
     * @param table the table that belongs to this query.
     */
    protected BaseModelQueriable(Class<TModel> table) {
        super(table);
        //noinspection unchecked
        retrievalAdapter = FlowManager.getInstanceAdapter(table);
    }

    @Override
    public CursorResult<TModel> queryResults() {
        return new CursorResult<>(retrievalAdapter.getModelClass(), query());
    }

    @Override
    public List<TModel> queryList() {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return retrievalAdapter.getListModelLoader().load(query);
    }

    @Override
    public TModel querySingle() {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return retrievalAdapter.getSingleModelLoader().load(query);
    }

    @Override
    public TModel querySingle(DatabaseWrapper wrapper) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return retrievalAdapter.getSingleModelLoader().load(wrapper, query);
    }

    @NonNull
    @Override
    public List<TModel> queryList(DatabaseWrapper wrapper) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        List<TModel> list = retrievalAdapter.getListModelLoader().load(wrapper, query);
        return list == null ? new ArrayList<TModel>() : list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <ModelContainerClass extends ModelContainer<TModel, ?>> ModelContainerClass queryModelContainer(@NonNull ModelContainerClass instance) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return (ModelContainerClass) FlowManager.getContainerAdapter(getTable())
                .getModelContainerLoader().load(query, instance);
    }

    @Override
    public FlowCursorList<TModel> cursorList() {
        return new FlowCursorList.Builder<>(getTable())
                .modelQueriable(this).build();
    }

    @Override
    public FlowQueryList<TModel> flowQueryList() {
        return new FlowQueryList.Builder<>(getTable())
                .modelQueriable(this)
                .build();
    }

    @Override
    public AsyncQuery<TModel> async() {
        return new AsyncQuery<>(this);
    }

    @Override
    public <QueryClass extends BaseQueryModel> List<QueryClass> queryCustomList(Class<QueryClass> queryModelClass) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return FlowManager.getQueryModelAdapter(queryModelClass).getListModelLoader().load(query);
    }

    @Override
    public <QueryClass extends BaseQueryModel> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return FlowManager.getQueryModelAdapter(queryModelClass).getSingleModelLoader().load(query);
    }
}
