package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: All modificatioon operations throw a {@link InvalidSqlViewOperationException}
 */
abstract class NoModificationModel implements Model {

    @Override
    public void save() {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not saveable");
    }

    @Override
    public void delete() {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not deleteable");
    }

    @Override
    public void update() {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not updateable");
    }

    @Override
    public void insert() {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not insertable");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean exists() {
        return getRetrievalAdapter().exists(this);
    }

    @SuppressWarnings("unchecked")
    public boolean exists(DatabaseWrapper databaseWrapper) {
        return getRetrievalAdapter().exists(this, databaseWrapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load() {
        getRetrievalAdapter().load(this);
    }

    @SuppressWarnings("unchecked")
    public void load(DatabaseWrapper databaseWrapper) {
        getRetrievalAdapter().load(this, databaseWrapper);
    }

    abstract RetrievalAdapter getRetrievalAdapter();

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    static class InvalidSqlViewOperationException extends RuntimeException {

        InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
