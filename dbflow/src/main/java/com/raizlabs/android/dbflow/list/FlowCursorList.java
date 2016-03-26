package com.raizlabs.android.dbflow.list;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;

import java.util.List;

/**
 * Description: A non-modifiable, cursor-backed list that you can use in {@link android.widget.ListView} or other data sources.
 */
public class FlowCursorList<TModel extends Model> {

    private Cursor cursor;
    private Class<TModel> table;
    private ModelCache<TModel, ?> modelCache;
    private boolean cacheModels;
    private ModelQueriable<TModel> modelQueriable;
    private int cacheSize;

    /**
     * Constructs an instance of this list with a specified cache size.
     *
     * @param cacheSize      The size of models to cache.
     * @param modelQueriable The SQL where query to use when doing a query.
     */
    public FlowCursorList(int cacheSize, ModelQueriable<TModel> modelQueriable) {
        this(false, modelQueriable);
        setCacheModels(true, cacheSize);
    }

    /**
     * Constructs an instance of this list.
     *
     * @param cacheModels    For every call to {@link #getItem(long)}, we want to keep a reference to it so
     *                       we do not need to convert the cursor data back into a {@link TModel} again.
     * @param modelQueriable The SQL where query to use when doing a query.
     */
    public FlowCursorList(boolean cacheModels, ModelQueriable<TModel> modelQueriable) {
        this.modelQueriable = modelQueriable;
        cursor = this.modelQueriable.query();
        table = modelQueriable.getTable();
        this.cacheModels = cacheModels;
        setCacheModels(cacheModels);
    }

    /**
     * Constructs an instance of this list.
     *
     * @param cacheModels For every call to {@link #getItem(long)}, do we want to keep a reference to it so
     *                    we do not need to convert the cursor data back into a {@link TModel} again.
     * @param table       The table to query from
     * @param conditions  The set of {@link SQLCondition} to query with
     */
    public FlowCursorList(boolean cacheModels, Class<TModel> table, SQLCondition... conditions) {
        this(cacheModels, new Select().from(table).where(conditions));
    }

    /**
     * Constructs an instance of this list with a specified cache size.
     *
     * @param cacheSize  The size of models to cache.
     * @param table      The table to query from
     * @param conditions The set of {@link SQLCondition} to query with
     */
    public FlowCursorList(int cacheSize, Class<TModel> table, SQLCondition... conditions) {
        this(false, new Select().from(table).where(conditions));
        setCacheModels(true, cacheSize);
    }

    /**
     * Sets this list to being caching models. If set to false, this will immediately clear the cache for you.
     * The cache size will default to the {@link android.database.Cursor#getCount()}
     *
     * @param cacheModels true, will cache models. If false, any and future caching is cleared.
     */
    public void setCacheModels(boolean cacheModels) {
        if (cacheModels) {
            throwIfCursorClosed();
            setCacheModels(true, cursor.getCount());
        } else {
            setCacheModels(false, cursor.getCount());
        }
    }

    /**
     * Sets this list to cache models. If set to false, it will immediately clear the cache for you.
     *
     * @param cacheModels true, will cache models. If false, any and future caching is cleared.
     * @param cacheSize   The size of models to cache.
     */
    public void setCacheModels(boolean cacheModels, int cacheSize) {
        this.cacheModels = cacheModels;
        if (!cacheModels) {
            clearCache();
        } else {
            throwIfCursorClosed();
            this.cacheSize = cacheSize;
            modelCache = getBackingCache();
        }
    }

    protected ModelCache<TModel, ?> getBackingCache() {
        return ModelLruCache.newInstance(cacheSize);
    }

    /**
     * Clears the {@link TModel} cache if we use a cache.
     */
    public void clearCache() {
        if (cacheModels) {
            modelCache.clear();
        }
    }

    /**
     * Refreshes the data backing this list, and destroys the Model cache.
     */
    public synchronized void refresh() {
        cursor.close();
        cursor = modelQueriable.query();

        if (cacheModels) {
            modelCache.clear();
            modelCache = getBackingCache();
        }
    }

    /**
     * Returns a model at the specified position. If we are using the cache and it does not contain a model
     * at that position, we move the cursor to the specified position and construct the {@link TModel}.
     *
     * @param position The row number in the {@link android.database.Cursor} to look at
     * @return The {@link TModel} converted from the cursor
     */
    public TModel getItem(long position) {
        throwIfCursorClosed();

        TModel model = null;
        if (cacheModels) {
            model = modelCache.get(position);
            if (model == null && cursor.moveToPosition((int) position)) {
                model = SqlUtils.convertToModel(true, table, cursor);
                modelCache.addModel(position, model);
            }
        } else if (cursor.moveToPosition((int) position)) {
            model = SqlUtils.convertToModel(true, table, cursor);
        }
        return model;
    }

    /**
     * @return the full, converted {@link TModel} list from the database on this list. For large
     * datasets that require a large conversion, consider calling this on a BG thread.
     */
    public List<TModel> getAll() {
        throwIfCursorClosed();
        return FlowManager.getModelAdapter(table).getListModelLoader().convertToData(cursor, null);
    }

    /**
     * @return the count of rows on this database query list.
     */
    public boolean isEmpty() {
        throwIfCursorClosed();
        return getCount() == 0;
    }

    /**
     * @return the count of the rows in the {@link android.database.Cursor} backed by this list.
     */
    public int getCount() {
        throwIfCursorClosed();
        return cursor != null ? cursor.getCount() : 0;
    }

    /**
     * Closes the cursor backed by this list
     */
    public void close() {
        cursor.close();
        cursor = null;
    }

    /**
     * @return The cursor backing this list.
     * @throws IllegalStateException when the cursor backing this list is closed.
     */
    public Cursor getCursor() {
        throwIfCursorClosed();
        return cursor;
    }

    public Class<TModel> getTable() {
        return table;
    }

    private void throwIfCursorClosed() {
        if (cursor == null || cursor.isClosed()) {
            throw new IllegalStateException("Cursor has been closed for FlowCursorList");
        }
    }

}
