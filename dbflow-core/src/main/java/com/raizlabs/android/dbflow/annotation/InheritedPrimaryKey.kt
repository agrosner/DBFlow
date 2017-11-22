package com.raizlabs.android.dbflow.annotation

/**
 * Description: Allows you to specify a non-Column to be inherited and used as a [PrimaryKey]
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class InheritedPrimaryKey(
        /**
         * @return The primary key annotation as if it was part of the class
         */
        val primaryKey: PrimaryKey,
        /**
         * @return The column annotation as if it was part of the class
         */
        val column: Column,
        /**
         * @return The field name that an inherited column uses. It must match exactly case-by-case to the field you're referencing.
         * If the field is private, the [PrimaryKey] allows you to define getter and setters for it.
         */
        val fieldName: String)
