package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.QueryModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

/**
 * Description: Provides a base implementation of {@link ModelQueriable} to simplify a lot of code. It provides the
 * default implementation for convenience.
 */
public abstract class BaseModelQueriable<TModel> extends BaseQueriable<TModel>
        implements ModelQueriable<TModel>, Query {

    private InstanceAdapter<TModel> retrievalAdapter;

    private boolean cachingEnabled = true;

    /**
     * Constructs new instance of this class and is meant for subclasses only.
     *
     * @param table the table that belongs to this query.
     */
    protected BaseModelQueriable(Class<TModel> table) {
        super(table);
    }

    private InstanceAdapter<TModel> getRetrievalAdapter() {
        if (retrievalAdapter == null) {
            retrievalAdapter = FlowManager.getInstanceAdapter(getTable());
        }
        return retrievalAdapter;
    }

    @NonNull
    @Override
    public CursorResult<TModel> queryResults() {
        return new CursorResult<>(getRetrievalAdapter().getModelClass(), query());
    }

    @NonNull
    @Override
    public List<TModel> queryList() {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return getListModelLoader().load(query);
    }

    @Nullable
    @Override
    public TModel querySingle() {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return getSingleModelLoader().load(query);
    }

    @Override
    public TModel querySingle(DatabaseWrapper wrapper) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return getSingleModelLoader().load(wrapper, query);
    }

    @NonNull
    @Override
    public List<TModel> queryList(DatabaseWrapper wrapper) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        return getListModelLoader().load(wrapper, query);
    }

    @NonNull
    @Override
    public FlowCursorList<TModel> cursorList() {
        return new FlowCursorList.Builder<>(getTable())
                .cacheModels(cachingEnabled)
                .modelQueriable(this).build();
    }

    @NonNull
    @Override
    public FlowQueryList<TModel> flowQueryList() {
        return new FlowQueryList.Builder<>(getTable())
                .cacheModels(cachingEnabled)
                .modelQueriable(this)
                .build();
    }

    @Override
    public long executeUpdateDelete() {
        return executeUpdateDelete(FlowManager.getWritableDatabaseForTable(getTable()));
    }

    @Override
    public long executeUpdateDelete(DatabaseWrapper databaseWrapper) {
        long affected = databaseWrapper.compileStatement(getQuery()).executeUpdateDelete();

        // only notify for affected.
        if (affected > 0) {
            SqlUtils.notifyTableChanged(getTable(), getPrimaryAction());
        }
        return affected;
    }

    @NonNull
    @Override
    public AsyncQuery<TModel> async() {
        return new AsyncQuery<>(this);
    }

    @NonNull
    @Override
    public <QueryClass> List<QueryClass> queryCustomList(Class<QueryClass> queryModelClass) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        QueryModelAdapter<QueryClass> adapter = FlowManager.getQueryModelAdapter(queryModelClass);
        return cachingEnabled
                ? adapter.getListModelLoader().load(query)
                : adapter.getNonCacheableListModelLoader().load(query);
    }

    @Nullable
    @Override
    public <QueryClass> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass) {
        String query = getQuery();
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query);
        QueryModelAdapter<QueryClass> adapter = FlowManager.getQueryModelAdapter(queryModelClass);
        return cachingEnabled
                ? adapter.getSingleModelLoader().load(query)
                : adapter.getNonCacheableSingleModelLoader().load(query);
    }

    @NonNull
    @Override
    public ModelQueriable<TModel> disableCaching() {
        cachingEnabled = false;
        return this;
    }

    private ListModelLoader<TModel> getListModelLoader() {
        return cachingEnabled
                ? getRetrievalAdapter().getListModelLoader()
                : getRetrievalAdapter().getNonCacheableListModelLoader();
    }

    private SingleModelLoader<TModel> getSingleModelLoader() {
        return cachingEnabled
                ? getRetrievalAdapter().getSingleModelLoader()
                : getRetrievalAdapter().getNonCacheableSingleModelLoader();
    }


}
