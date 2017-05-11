package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: A convenience class for {@link ReadOnlyModel}.
 */
abstract class NoModificationModel implements ReadOnlyModel {

    private transient RetrievalAdapter adapter;

    @SuppressWarnings("unchecked")
    public boolean exists() {
        return getRetrievalAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public boolean exists(DatabaseWrapper databaseWrapper) {
        return getRetrievalAdapter().exists(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    public void load() {
        getRetrievalAdapter().load(this);
    }

    @SuppressWarnings("unchecked")
    public void load(DatabaseWrapper wrapper) {
        getRetrievalAdapter().load(this, wrapper);
    }

    public RetrievalAdapter getRetrievalAdapter() {
        if (adapter == null) {
            adapter = FlowManager.getInstanceAdapter(getClass());
        }
        return adapter;
    }

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    static class InvalidSqlViewOperationException extends RuntimeException {

        InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
