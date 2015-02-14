package com.raizlabs.android.dbflow.list;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.ModelQueriable;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;

import java.util.List;

/**
 * Author: andrewgrosner
 * Description: A non-modifiable, cursor-backed list that you can use in {@link android.widget.ListView} or other data sources.
 */
public class FlowCursorList<ModelClass extends Model> {

    private Cursor mCursor;

    private Class<ModelClass> mTable;

    private ModelCache<ModelClass, ?> mModelCache;

    private boolean cacheModels;

    private ModelQueriable<ModelClass> mQueriable;

    private CursorObserver mObserver;

    private int mCacheSize;

    /**
     * Constructs an instance of this list with a specified cache size.
     *
     * @param cacheSize      The size of models to cache.
     * @param modelQueriable The SQL where query to use when doing a query.
     */
    public FlowCursorList(int cacheSize, ModelQueriable<ModelClass> modelQueriable) {
        this(false, modelQueriable);
        setCacheModels(true, cacheSize);
    }

    /**
     * Constructs an instance of this list.
     *
     * @param cacheModels    For every call to {@link #getItem(long)}, we want to keep a reference to it so
     *                       we do not need to convert the cursor data back into a {@link ModelClass} again.
     * @param modelQueriable The SQL where query to use when doing a query.
     */
    public FlowCursorList(boolean cacheModels, ModelQueriable<ModelClass> modelQueriable) {
        mQueriable = modelQueriable;
        mCursor = mQueriable.query();
        mTable = modelQueriable.getTable();
        this.cacheModels = cacheModels;
        mCursor.registerContentObserver(mObserver = new CursorObserver());
        setCacheModels(cacheModels);
    }

    /**
     * Constructs an instance of this list.
     *
     * @param cacheModels For every call to {@link #getItem(long)}, do we want to keep a reference to it so
     *                    we do not need to convert the cursor data back into a {@link ModelClass} again.
     * @param table       The table to query from
     * @param conditions  The set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to query with
     */
    public FlowCursorList(boolean cacheModels, Class<ModelClass> table, Condition... conditions) {
        this(cacheModels, new Select().from(table).where(conditions));
    }

    /**
     * Constructs an instance of this list with a specified cache size.
     *
     * @param cacheSize  The size of models to cache.
     * @param table      The table to query from
     * @param conditions The set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to query with
     */
    public FlowCursorList(int cacheSize, Class<ModelClass> table, Condition... conditions) {
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
            setCacheModels(true, mCursor.getCount());
        } else {
            setCacheModels(false, mCursor.getCount());
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
            mCacheSize = cacheSize;
            mModelCache = getBackingCache();
        }
    }

    protected ModelCache<ModelClass, ?> getBackingCache() {
        return new ModelLruCache<>(mCacheSize);
    }

    /**
     * Clears the {@link ModelClass} cache if we use a cache.
     */
    public void clearCache() {
        if (cacheModels) {
            mModelCache.clear();
        }
    }

    /**
     * Refreshes the data backing this list, and destroys the Model cache.
     */
    public synchronized void refresh() {
        mCursor.close();
        mCursor = mQueriable.query();

        if (cacheModels) {
            mModelCache.clear();
            mModelCache = getBackingCache();
        }
    }

    /**
     * Returns a model at the specified position. If we are using the cache and it does not contain a model
     * at that position, we move the cursor to the specified position and construct the {@link ModelClass}.
     *
     * @param position The row number in the {@link android.database.Cursor} to look at
     * @return The {@link ModelClass} converted from the cursor
     */
    public ModelClass getItem(long position) {
        throwIfCursorClosed();

        ModelClass model = null;
        if (cacheModels) {
            model = mModelCache.get(position);
            if (model == null && mCursor.moveToPosition((int) position)) {
                model = SqlUtils.convertToModel(true, mTable, mCursor);
                mModelCache.addModel(position, model);
            }
        } else if (mCursor.moveToPosition((int) position)) {
            model = SqlUtils.convertToModel(true, mTable, mCursor);
        }
        return model;
    }

    /**
     * Fetches the list on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}. For
     * large data sets this will take some time.
     *
     * @param transactionListener Called when we retrieve the results.
     */
    public void fetchAll(TransactionListener<List<ModelClass>> transactionListener) {
        throwIfCursorClosed();
        TransactionManager.getInstance().addTransaction(
                new BaseResultTransaction<List<ModelClass>>(DBTransactionInfo.createFetch(), transactionListener) {
                    @Override
                    public List<ModelClass> onExecute() {
                        return getAll();
                    }
                });
    }

    /**
     * @return the full, converted {@link ModelClass} list from the database on this list. For very
     * large datasets, it's not encouraged to use this method. Use {@link #fetchAll(com.raizlabs.android.dbflow.runtime.transaction.TransactionListener)}
     * instead.
     */
    public List<ModelClass> getAll() {
        throwIfCursorClosed();
        return SqlUtils.convertToList(mTable, mCursor);
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
        return mCursor != null ? mCursor.getCount() : 0;
    }

    /**
     * Closes the cursor backed by this list
     */
    public void close() {
        mCursor.unregisterContentObserver(mObserver);
        mCursor.close();
        mObserver = null;
        mCursor = null;
    }

    public Class<ModelClass> getTable() {
        return mTable;
    }

    private void throwIfCursorClosed() {
        if (mCursor == null || mCursor.isClosed()) {
            throw new IllegalStateException("Cursor has been closed for FlowCursorList");
        }
    }

    class CursorObserver extends ContentObserver {

        CursorObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null && !mCursor.isClosed()) {
                refresh();
            }
        }
    }
}
