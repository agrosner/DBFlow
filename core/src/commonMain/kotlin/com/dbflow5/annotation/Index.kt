package com.dbflow5.annotation

/**
 * Description: Creates an index for a specified [Column]. A single column can belong to multiple
 * indexes within the same table if you wish.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class Index(
        /**
         * @return The set of index groups that this index belongs to.
         */
        val indexGroups: IntArray = [])
