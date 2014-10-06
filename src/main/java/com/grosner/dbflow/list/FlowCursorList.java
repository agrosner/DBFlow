package com.grosner.dbflow.list;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.SparseArray;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.BaseResultTransaction;
import com.grosner.dbflow.runtime.transaction.QueryTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.Select;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: A cursor backed list that you can use in {@link android.widget.ListView} or other data sources.
 */
public class FlowCursorList<ModelClass extends Model> {

    private Cursor mCursor;

    private FlowManager mManager;

    private Class<ModelClass> mTable;

    private SparseArray<ModelClass> mModelCache;

    private boolean cacheModels;

    private Where<ModelClass> mWhere;

    /**
     * Constructs an instance of this list.
     *
     * @param cacheModels For every call to {@link #getItem(int)}, do we want to keep a reference to it so
     *                    we do not need to convert the cursor data back into a {@link ModelClass} again.
     * @param flowManager The database manager to use
     * @param table       The table to query from
     * @param conditions  The set of {@link com.grosner.dbflow.sql.builder.Condition} to query with
     */
    public FlowCursorList(boolean cacheModels, FlowManager flowManager, Class<ModelClass> table, Condition... conditions) {
        this.cacheModels = cacheModels;
        mWhere = new Select(flowManager).from(table).where(conditions);
        mCursor = mWhere.query();
        mManager = flowManager;
        mTable = table;

        if(cacheModels) {
            mModelCache = new SparseArray<ModelClass>(mCursor.getCount());
        }

        mCursor.registerContentObserver(new CursorObserver());
    }

    /**
     * Constructs an instance of this list with the shared {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param cacheModels For every call to {@link #getItem(int)}, do we want to keep a reference to it so
     *                    we do not need to convert the cursor data back into a {@link ModelClass} again.
     * @param table       The table to query from
     * @param conditions  The set of {@link com.grosner.dbflow.sql.builder.Condition} to query with
     */
    public FlowCursorList(boolean cacheModels, Class<ModelClass> table, Condition... conditions) {
        this(cacheModels, FlowManager.getInstance(), table, conditions);
    }

    /**
     * @return the count of the rows in the {@link android.database.Cursor} backed by this list.
     */
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    /**
     * Returns a model at the specified position. If we are using the cache and it does not contain a model
     * at that position, we move the cursor to the specified position and construct the {@link ModelClass}.
     *
     * @param position The row number in the {@link android.database.Cursor} to look at
     * @return The {@link ModelClass} converted from the cursor
     */
    public ModelClass getItem(int position) {
        ModelClass model;
        if (cacheModels) {
            model = mModelCache.get(position);
            if (model == null && mCursor.moveToPosition(position)) {
                model = SqlUtils.convertToModel(false, mManager, mTable, mCursor);
                mModelCache.put(position, model);
            }
        } else {
            mCursor.moveToPosition(position);
            model = SqlUtils.convertToModel(false, mManager, mTable, mCursor);
        }
        return model;
    }

    /**
     * Returns the full, converted {@link ModelClass} list from the database on this list.
     *
     * @return
     */
    public List<ModelClass> getAll() {
        return SqlUtils.convertToList(mManager, mTable, mCursor);
    }

    /**
     * Fetches the query on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     * @param resultReceiver Called when we retrieve the results.
     */
    public void fetchAll(ResultReceiver<List<ModelClass>> resultReceiver) {
        TransactionManager.getInstance().addTransaction(
                new BaseResultTransaction<List<ModelClass>>(DBTransactionInfo.createFetch(), resultReceiver) {
                @Override
                public List<ModelClass> onExecute() {
                    return getAll();
                }
        });
    }

    /**
     * @return the count of rows on this database query list.
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * Closes the cursor backed by this list
     */
    public void close() {
        mCursor.close();
    }

    public FlowManager getManager() {
        return mManager;
    }

    public Class<ModelClass> getTable() {
        return mTable;
    }

    class CursorObserver extends ContentObserver {

        CursorObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
                mCursor = mWhere.query();

                // clearing cache as data has changed
                if(cacheModels) {
                    mModelCache.clear();
                }
            }
        }
    }
}
