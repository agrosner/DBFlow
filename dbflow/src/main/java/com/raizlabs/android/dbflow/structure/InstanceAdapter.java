package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;

/**
 * Description: Provides a {@link #newInstance()} method to a {@link RetrievalAdapter}
 */
@SuppressWarnings("NullableProblems")
public abstract class InstanceAdapter<TModel>
    extends RetrievalAdapter<TModel> {

    public InstanceAdapter(@NonNull DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
    }

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    @NonNull
    public abstract TModel newInstance();
}
