package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: Provides a base implementation for a ModelView. Use a {@link com.raizlabs.android.dbflow.annotation.ModelView}
 * annotation to register it properly.
 */
public abstract class BaseModelView<ModelClass extends Model> implements Model {

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
        return ((ModelViewAdapter<? extends Model, BaseModelView<ModelClass>>) FlowManager.getModelViewAdapter(getClass())).exists(this);
    }

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    private static class InvalidSqlViewOperationException extends RuntimeException {

        private InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
