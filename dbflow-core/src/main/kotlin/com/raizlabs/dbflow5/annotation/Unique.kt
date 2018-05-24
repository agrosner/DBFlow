package com.raizlabs.dbflow5.annotation

/**
 * Description: Marks the field as unique, meaning its value cannot be repeated. It is, however,
 * NOT a primary key.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class Unique(
    /**
     * @return if field is unique. If false, we expect [.uniqueGroups] to be specified.`
     */
    val unique: Boolean = true,
    /**
     * @return Marks a unique field as part of a unique group. For every unique number entered,
     * it will generate a UNIQUE() column statement.
     */
    val uniqueGroups: IntArray = [],
    /**
     * Defines how to handle conflicts for a unique column
     *
     * @return a [ConflictAction] enum
     */
    val onUniqueConflict: ConflictAction = ConflictAction.FAIL)
