package com.raizlabs.android.dbflow.structure;

/**
 * Description: Provides a {@link #newInstance()} method to a {@link com.raizlabs.android.dbflow.structure.RetrievalAdapter}
 */
public interface InstanceAdapter<TableClass extends Model, ModelClass extends Model>
        extends RetrievalAdapter<TableClass, ModelClass> {

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public ModelClass newInstance();
}
