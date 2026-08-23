package com.dbflow5.annotation

/**
 * Description:
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UniqueGroup(
        /**
         * @return The number that columns point to to use this group
         */
        val groupNumber: Int,
        /**
         * @return The conflict action that this group takes.
         */
        val uniqueConflict: ConflictAction = ConflictAction.FAIL)
