package com.dbflow5.annotation

import kotlin.reflect.KClass

/**
 * Description:
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ForeignKey(
    /**
     * Defines explicit references for a composite [ForeignKey] definition. This is no longer required
     * as the library will auto-generate references for you based on the other table's primary keys.
     *
     * @return the set of explicit references if you wish to have different values than default generated.
     */
    val references: Array<ForeignKeyReference> = [],
    /**
     * @return Default false. When this column is a [ForeignKey] and table object,
     * returning true will save the model before adding the fields to save as a foreign key.
     * If false, we expect the field to not change and must save the model manually outside
     * of the ModelAdapter before saving the child class.
     */
    val saveForeignKeyModel: Boolean = false,
    /**
     * @return This method instructs the code generator to only
     * populate the model with the [ForeignKeyReference] defined in this field. This skips
     * the Select retrieval convenience.
     */
    @DBFlowKAPTOnly
    @Deprecated(
        "This property does not work with immutable models and encourages main " +
            "thread reading. Replace with basic fields for foreign key and use @OneToManyRelation" +
            " to load the full models from the DB."
    )
    val stubbedRelationship: Boolean = false,
    /**
     * @return If true, during a transaction, FK constraints are not violated immediately until the resulting transaction commits.
     * This is useful for out of order foreign key operations.
     * @see [Deferred Foreign Key Constraints](http://www.sqlite.org/foreignkeys.html.fk_deferred)
     */
    val deferred: Boolean = false,
    /**
     * @return an optional table class that this reference points to. It's only used if the field
     * is NOT a Model class.
     */
    val tableClass: KClass<*> = Any::class,
    /**
     * Defines [ForeignKeyAction] action to be performed
     * on delete of referenced record. Defaults to [ForeignKeyAction.NO_ACTION]. Used only when
     * columnType is [ForeignKey].
     *
     * @return [ForeignKeyAction]
     */
    val onDelete: ForeignKeyAction = ForeignKeyAction.NO_ACTION,
    /**
     * Defines [ForeignKeyAction] action to be performed
     * on update of referenced record. Defaults to [ForeignKeyAction.NO_ACTION]. Used only when
     * columnType is [ForeignKey].
     *
     * @return [ForeignKeyAction]
     */
    val onUpdate: ForeignKeyAction = ForeignKeyAction.NO_ACTION
)
