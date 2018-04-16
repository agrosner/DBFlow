package com.raizlabs.dbflow5.annotation

/**
 * Description: marks a single field as a ModelCache creator that is used in the corresponding ModelAdapter.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelCacheField
