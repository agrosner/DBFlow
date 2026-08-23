package com.dbflow5.annotation

/**
 * Description: Maps an arbitrary object and its corresponding fields into a set of columns. It is similar
 * to [ForeignKey] except it's not represented in the DB hierarchy.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class ColumnMap(
        /**
         * Defines explicit references for a composite [ColumnMap] definition.
         *
         * @return override explicit usage of all fields and provide custom references.
         */
        val references: Array<ColumnMapReference> = [])
