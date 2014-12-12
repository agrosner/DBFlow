package com.raizlabs.android.dbflow.structure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface InternalAdapter<ModelClass extends Model> {

    public abstract Class<ModelClass> getModelClass();

    public abstract String getTableName();

}
