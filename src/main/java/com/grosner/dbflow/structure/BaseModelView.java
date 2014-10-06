package com.grosner.dbflow.structure;

import android.database.Cursor;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.sql.Where;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation for a {@link com.grosner.dbflow.structure.ModelView}.
 */
public abstract class BaseModelView<ModelClass extends Model> implements ModelView<ModelClass>, Model {

    /**
     * Gets thrown when an operation is not valid for the SQL {@link com.grosner.dbflow.structure.ModelView}
     */
    private static class InvalidSqlViewOperationException extends RuntimeException {

        private InvalidSqlViewOperationException(String detailMessage) {
            super(detailMessage);
        }
    }

    @Override
    public abstract Where<ModelClass> getWhere();

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void save(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getName() + " is not saveable");
    }

    @Override
    public void delete(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getName() + " is not deleteable");
    }

    @Override
    public void update(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getName() + " is not updateable");
    }

    @Override
    public void insert(boolean async) {
        throw new InvalidSqlViewOperationException("View " + getName() + " is not insertable");
    }

    @Override
    public void load(Cursor cursor) {
        SqlUtils.loadFromCursor(FlowManager.getInstance(), this, cursor);
    }

    @Override
    public boolean exists() {
        return SqlUtils.exists(FlowManager.getInstance(), this);
    }
}
