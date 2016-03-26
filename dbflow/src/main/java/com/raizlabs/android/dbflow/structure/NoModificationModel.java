package com.raizlabs.android.dbflow.structure;

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

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    static class InvalidSqlViewOperationException extends RuntimeException {

        InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
