package com.raizlabs.dbflow5.annotation

import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.ConflictAction

/**
 * Description: Allows [Table] to inherit fields from other objects to make it part of the DB table.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class InheritedColumn(
        /**
         * @return The column annotation as if it was part of the class
         */
        val column: Column,
        /**
         * @return The field name that an inherited column uses. It must match exactly case-by-case to the field you're referencing.
         * If the field is private, the [Column] allows you to define getter and setters for it.
         */
        val fieldName: String,
        /**
         * @return If specified other than [ConflictAction.NONE], then we assume [NotNull].
         */
        val nonNullConflict: ConflictAction = ConflictAction.NONE)
