package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;

/**
 * Description: Provides a {@link #newInstance()} method to a {@link RetrievalAdapter}
 */
public abstract class InstanceAdapter<TModel extends Model, TTable extends Model>
        extends RetrievalAdapter<TModel, TTable> {

    public InstanceAdapter(DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
    }

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    public abstract TModel newInstance();
}
