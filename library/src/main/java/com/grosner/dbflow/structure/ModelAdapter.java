package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ModelAdapter<ModelClass extends Model> {

    protected String mCreationQuery;

    protected String mPrimaryWhere;

    public abstract ModelClass loadFromCursor(Cursor cursor);

    public abstract void save(boolean async, ModelClass model, int saveMode);

    public abstract boolean exists(ModelClass model);

    public abstract boolean delete(ModelClass model);

    public abstract String getPrimaryModelWhere(ModelClass model);

    public abstract String getPrimaryModelWhere();

    public abstract String getCreationQuery();

    public abstract Class<ModelClass> getModelClass();

    public abstract String getTableName();
}
