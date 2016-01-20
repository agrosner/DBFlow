package com.raizlabs.android.dbflow.structure;

/**
 * Description: Provides a {@link #newInstance()} method to a {@link RetrievalAdapter}
 */
public abstract class InstanceAdapter<ModelClass extends Model, TableClass extends Model>
        extends RetrievalAdapter<ModelClass, TableClass> {

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public abstract ModelClass newInstance();
}
