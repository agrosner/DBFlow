package com.raizlabs.dbflow5.annotation

/**
 * Description: Marks a field as the IMultiKeyCacheModel that we use to convert multiple fields into
 * a single key for caching.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class MultiCacheField
