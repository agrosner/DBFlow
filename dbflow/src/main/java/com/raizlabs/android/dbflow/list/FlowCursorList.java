package com.raizlabs.android.dbflow.list;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Description: A non-modifiable, cursor-backed list that you can use in {@link ListView} or other data sources.
 */
public class FlowCursorList<TModel extends Model> {

    /**
     * Interface for callbacks when cursor gets refreshed.
     */
    public interface OnCursorRefreshListener<TModel extends Model> {

        /**
         * Callback when cursor refreshes.
         *
         * @param cursorList The object that changed.
         */
        void onCursorRefreshed(FlowCursorList<TModel> cursorList);
    }

    /**
     * The default size of the cache if cache size is 0 or not specified.
     */
    public static final int DEFAULT_CACHE_SIZE = 50;

    /**
     * Minimum size that we make the cache (if size is supported in cache)
     */
    public static final int MIN_CACHE_SIZE = 20;

    @Nullable
    private Cursor cursor;

    private Class<TModel> table;
    private ModelCache<TModel, ?> modelCache;
    private boolean cacheModels;
    private ModelQueriable<TModel> modelQueriable;
    private int cacheSize;
    private ModelAdapter<TModel> modelAdapter;

    private final java.util.Set<OnCursorRefreshListener<TModel>> cursorRefreshListenerSet = new HashSet<>();

    public FlowCursorList(ModelQueriable<TModel> modelQueriable) {
        this(true, modelQueriable);
    }

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
        modelAdapter = FlowManager.getModelAdapter(table);
        this.cacheModels = cacheModels;
        setCacheModels(cacheModels);
    }

    /**
     * Register listener for when cursor refreshes.
     */
    public void addOnCursorRefreshListener(OnCursorRefreshListener<TModel> onCursorRefreshListener) {
        synchronized (cursorRefreshListenerSet) {
            cursorRefreshListenerSet.add(onCursorRefreshListener);
        }
    }

    public void removeOnCursorRefreshListener(OnCursorRefreshListener<TModel> onCursorRefreshListener) {
        synchronized (cursorRefreshListenerSet) {
            cursorRefreshListenerSet.remove(onCursorRefreshListener);
        }
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
            setCacheModels(true, cursor == null ? 0 : cursor.getCount());
        } else {
            setCacheModels(false, cursor == null ? 0 : cursor.getCount());
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
            if (cacheSize <= MIN_CACHE_SIZE) {
                if (cacheSize == 0) {
                    cacheSize = DEFAULT_CACHE_SIZE;
                } else {
                    cacheSize = MIN_CACHE_SIZE;
                }
            }
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
        warnEmptyCursor();
        if (cursor != null) {
            cursor.close();
        }
        cursor = modelQueriable.query();

        if (cacheModels) {
            modelCache.clear();
            setCacheModels(true, cursor == null ? 0 : cursor.getCount());
        }

        synchronized (cursorRefreshListenerSet) {
            for (OnCursorRefreshListener<TModel> listener : cursorRefreshListenerSet) {
                listener.onCursorRefreshed(this);
            }
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
        warnEmptyCursor();

        TModel model = null;
        if (cacheModels) {
            model = modelCache.get(position);
            if (model == null && cursor != null && cursor.moveToPosition((int) position)) {
                model = modelAdapter.getSingleModelLoader().convertToData(cursor, null, false);
                modelCache.addModel(position, model);
            }
        } else if (cursor != null && cursor.moveToPosition((int) position)) {
            model = modelAdapter.getSingleModelLoader().convertToData(cursor, null, false);
        }
        return model;
    }

    /**
     * @return the full, converted {@link TModel} list from the database on this list. For large
     * datasets that require a large conversion, consider calling this on a BG thread.
     */
    public List<TModel> getAll() {
        throwIfCursorClosed();
        warnEmptyCursor();
        return cursor == null ? new ArrayList<TModel>() :
            FlowManager.getModelAdapter(table).getListModelLoader().convertToData(cursor, null);
    }

    /**
     * @return the count of rows on this database query list.
     */
    public boolean isEmpty() {
        throwIfCursorClosed();
        warnEmptyCursor();
        return getCount() == 0;
    }

    /**
     * @return the count of the rows in the {@link android.database.Cursor} backed by this list.
     */
    public int getCount() {
        throwIfCursorClosed();
        warnEmptyCursor();
        return cursor != null ? cursor.getCount() : 0;
    }

    /**
     * Closes the cursor backed by this list
     */
    public void close() {
        warnEmptyCursor();
        if (cursor != null) {
            cursor.close();
        }
        cursor = null;
    }

    /**
     * @return The cursor backing this list.
     * @throws IllegalStateException when the cursor backing this list is closed.
     */
    @Nullable
    public Cursor getCursor() {
        throwIfCursorClosed();
        warnEmptyCursor();
        return cursor;
    }

    public Class<TModel> getTable() {
        return table;
    }

    private void throwIfCursorClosed() {
        if (cursor != null && cursor.isClosed()) {
            throw new IllegalStateException("Cursor has been closed for FlowCursorList");
        }
    }

    private void warnEmptyCursor() {
        if (cursor == null) {
            FlowLog.log(FlowLog.Level.W, "Cursor was null for FlowCursorList");
        }
    }

}
