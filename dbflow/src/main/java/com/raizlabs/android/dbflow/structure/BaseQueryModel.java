package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: Provides a base class for objects that represent {@link QueryModel}.
 */
public class BaseQueryModel extends NoModificationModel {

    private transient QueryModelAdapter adapter;

    @Override
    public boolean exists() {
        throw new InvalidSqlViewOperationException("Query " + getClass().getName() + " does not exist as a table." +
                "It's a convenient representation of a complex SQLite query.");
    }

    public QueryModelAdapter getQueryModelAdapter() {
        if (adapter == null) {
            adapter = FlowManager.getQueryModelAdapter(getClass());
        }
        return adapter;
    }

    /**
     * Load the cursor from a query into this model. No validation required. It will attempt
     * to fill any available properties into the model.
     *
     * @param cursor The cursor to load into this object.
     */
    @SuppressWarnings("unchecked")
    public void loadFromCursor(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            getQueryModelAdapter().loadFromCursor(cursor, this);
        }

        if (cursor != null) {
            cursor.close();
        }
    }
}
