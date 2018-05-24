package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description: Builds a many-to-many relationship with another [Table]. Only one table needs to specify
 * the annotation and its assumed that they use primary keys only. The generated
 * class will contain an auto-incrementing primary key by default.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class ManyToMany(
        /**
         * @return The other table class by which this will get merged.
         */
        val referencedTable: KClass<*>,
        /**
         * @return A name that we use as the column name for the referenced table in the
         * generated ManyToMany table class.
         */
        val referencedTableColumnName: String = "",
        /**
         * @return A name that we use as the column name for this specific table's name.
         */
        val thisTableColumnName: String = "",
        /**
         * @return By default, we generate an auto-incrementing [Long] [PrimaryKey].
         * If false, all [PrimaryKey] of the corresponding tables will be placed as [ForeignKey] and [PrimaryKey]
         * of the generated table instead of using an autoincrementing Long [PrimaryKey].
         */
        val generateAutoIncrement: Boolean = true,
        /**
         * @return by default, we append {selfTable}{generatedClassSeparator}{referencedTable} or "User_Follower",
         * for example. If you want different name, change this.
         */
        val generatedTableClassName: String = "",
        /**
         * @return by default the Models referenced here are not saved prior to saving this
         * object for obvious efficiency reasons.
         * @see ForeignKey.saveForeignKeyModel
         */
        val saveForeignKeyModels: Boolean = false)
