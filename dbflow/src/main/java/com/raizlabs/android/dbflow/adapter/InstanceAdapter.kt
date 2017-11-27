package com.raizlabs.android.dbflow.adapter

import com.raizlabs.android.dbflow.config.DatabaseDefinition

/**
 * Description: Provides a [.newInstance] method to a [RetrievalAdapter]
 */
abstract class InstanceAdapter<T : Any>(databaseDefinition: DatabaseDefinition)
    : RetrievalAdapter<T>(databaseDefinition) {

    /**
     * @return A new model using its default constructor. This is why default is required so that
     * we don't use reflection to create objects = faster.
     */
    abstract fun newInstance(): T
}
