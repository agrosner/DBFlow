package com.raizlabs.android.dbflow.annotation

const val GENERIC = -1

/**
 * Description:
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class IndexGroup(
        /**
         * @return The number that each contained [Index] points to, so they can be combined into a single index.
         * If [.GENERIC], this will assume a generic index that covers the whole table.
         */
        val number: Int = GENERIC,
        /**
         * @return The name of this index. It must be unique from other [IndexGroup].
         */
        val name: String,
        /**
         * @return If true, this will disallow duplicate values to be inserted into the table.
         */
        val unique: Boolean = false)

