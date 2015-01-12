package com.raizlabs.android.dbflow.structure;

/**
 * Description: Used for all our internal Adapter classes such as generated {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
 * or {@link com.raizlabs.android.dbflow.annotation.ContainerAdapter}
 */
public interface InternalAdapter<ModelClass extends Model> {

    /**
     * @return the model class this adapter corresponds to
     */
    public abstract Class<ModelClass> getModelClass();

    /**
     * @return The table name of this adapter.
     */
    public abstract String getTableName();

}
