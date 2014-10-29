package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface InternalAdapter<ModelClass extends Model> {

    public abstract Class<ModelClass> getModelClass();

    public abstract String getTableName();

}
