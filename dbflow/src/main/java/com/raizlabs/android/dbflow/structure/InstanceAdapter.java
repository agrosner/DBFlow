package com.raizlabs.android.dbflow.structure;

/**
 * Description: Provides a {@link #newInstance()} method to a {@link RetrievalAdapter}
 */
public abstract class InstanceAdapter<TModel extends Model, TTable extends Model>
        extends RetrievalAdapter<TModel, TTable> {

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public abstract TModel newInstance();
}
