package com.grosner.dbflow.structure;

import android.database.Cursor;

import com.grosner.dbflow.sql.SqlUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation for a ModelView. Define a {@link com.grosner.dbflow.structure.ModelViewDefinition} to describe
 * how to create the model view.
 */
@Ignore
public abstract class BaseModelView<ModelClass extends Model> implements Model {

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    private static class InvalidSqlViewOperationException extends RuntimeException {

        private InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }

    @Override
    public void save(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not saveable");
    }

    @Override
    public void delete(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not deleteable");
    }

    @Override
    public void update(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not updateable");
    }

    @Override
    public void insert(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getClass().getName() + " is not insertable");
    }

    @Override
    public void load(Cursor cursor) {
        SqlUtils.loadFromCursor(this, cursor);
    }

    @Override
    public boolean exists() {
        return SqlUtils.exists(this);
    }
}
