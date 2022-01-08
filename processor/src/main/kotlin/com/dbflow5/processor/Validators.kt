package com.dbflow5.processor

import com.dbflow5.processor.definition.DatabaseDefinition
import com.dbflow5.processor.definition.ModelViewDefinition
import com.dbflow5.processor.definition.OneToManyDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.EnumColumnAccessor
import com.dbflow5.processor.definition.column.PrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.isNullOrEmpty


/**
 * Description: the base interface for validating annotations.
 */
interface Validator<in ValidatorDefinition> {

    /**
     * @param processorManager    The manager
     * *
     * @param validatorDefinition The validator to use
     * *
     * @return true if validation passed, false if there was an error.
     */
    fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: ValidatorDefinition
    ): Boolean
}


/**
 * Description: Ensures the integrity of the annotation processor for columns.
 * @author Andrew Grosner (fuzz)
 */
class ColumnValidator : Validator<ColumnDefinition> {

    private var autoIncrementingPrimaryKey: ColumnDefinition? = null

    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: ColumnDefinition
    ): Boolean {

        var success = true

        // validate getter and setters.
        if (validatorDefinition.columnAccessor is PrivateScopeColumnAccessor) {
            val privateColumnAccess =
                validatorDefinition.columnAccessor as PrivateScopeColumnAccessor
            if (!validatorDefinition.entityDefinition.classElementLookUpMap.containsKey(
                    privateColumnAccess.getterNameElement
                )
            ) {
                processorManager.logError(
                    ColumnValidator::class,
                    """Could not find getter for private element: "${validatorDefinition.elementName}"
                            | from table class: ${validatorDefinition.entityDefinition.elementName}.
                            | Consider adding a getter with name ${privateColumnAccess.getterNameElement},
                            |  making it more accessible, or adding a @get:JvmName("${privateColumnAccess.getterNameElement}") """
                        .trimMargin()
                )
                success = false
            }
            if (!validatorDefinition.entityDefinition.classElementLookUpMap.containsKey(
                    privateColumnAccess.setterNameElement
                )
            ) {
                processorManager.logError(
                    ColumnValidator::class,
                    """Could not find setter for private element: "${validatorDefinition.elementName}"
                            | from table class: ${validatorDefinition.entityDefinition.elementName}.
                            | Consider adding a setter with name ${privateColumnAccess.setterNameElement},
                            | making it more accessible, or adding a @set:JvmName("${privateColumnAccess.setterNameElement}")."""
                        .trimMargin()
                )
                success = false
            }
        }

        if (!validatorDefinition.defaultValue.isNullOrEmpty()) {
            val typeName = validatorDefinition.elementTypeName
            if (validatorDefinition is ReferenceColumnDefinition && validatorDefinition.isReferencingTableObject) {
                processorManager.logError(
                    ColumnValidator::class,
                    "Default values cannot be specified for model fields"
                )
            } else if (typeName?.isPrimitive == true) {
                processorManager.logWarning(
                    ColumnValidator::class,
                    "Default value of ${validatorDefinition.defaultValue} from" +
                        " ${validatorDefinition.entityDefinition.elementName}.${validatorDefinition.elementName}" +
                        " is ignored for primitive columns."
                )
            }
        }

        if (validatorDefinition.columnName.isEmpty()) {
            success = false
            processorManager.logError(
                "Field ${validatorDefinition.elementName} " +
                    "cannot have a null column name for column: ${validatorDefinition.columnName}" +
                    " and type: ${validatorDefinition.elementTypeName}"
            )
        }

        if (validatorDefinition.columnAccessor is EnumColumnAccessor) {
            if (validatorDefinition.type is ColumnDefinition.Type.Primary) {
                success = false
                processorManager.logError(
                    "Enums cannot be primary keys. Column: ${validatorDefinition.columnName}" +
                        " and type: ${validatorDefinition.elementTypeName}"
                )
            } else if (validatorDefinition is ReferenceColumnDefinition) {
                success = false
                processorManager.logError(
                    "Enums cannot be foreign keys. Column: ${validatorDefinition.columnName}" +
                        " and type: ${validatorDefinition.elementTypeName}"
                )
            }
        }

        if (validatorDefinition is ReferenceColumnDefinition) {
            validatorDefinition.column?.let {
                if (it.name.isNotEmpty()) {
                    success = false
                    processorManager.logError(
                        "Foreign Key ${validatorDefinition.elementName} cannot specify the @Column.name() field. "
                            + "Use a @ForeignKeyReference(columnName = {NAME} instead. " +
                            "Column: ${validatorDefinition.columnName} and type: ${validatorDefinition.elementTypeName}"
                    )
                }
            }

            // it is an error to specify both a not null and provide explicit references.
            if (validatorDefinition.explicitReferences && validatorDefinition.notNull) {
                success = false
                processorManager.logError(
                    "Foreign Key ${validatorDefinition.elementName} " +
                        "cannot specify both @NotNull and references. Remove the top-level @NotNull " +
                        "and use the contained 'notNull' field " +
                        "in each reference to control its SQL notnull conflicts."
                )
            }

        } else {
            if (autoIncrementingPrimaryKey != null && validatorDefinition.type is ColumnDefinition.Type.Primary) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.")
                success = false
            }

            if (validatorDefinition.type is ColumnDefinition.Type.PrimaryAutoIncrement
                || validatorDefinition.type is ColumnDefinition.Type.RowId
            ) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = validatorDefinition
                } else if (autoIncrementingPrimaryKey != validatorDefinition) {
                    processorManager.logError(
                        "Only one auto-incrementing primary key is allowed on a table. " +
                            "Found Column: ${validatorDefinition.columnName} and type: ${validatorDefinition.elementTypeName}"
                    )
                    success = false
                }
            }
        }

        return success
    }
}

/**
 * Description:
 */
class DatabaseValidator : Validator<DatabaseDefinition> {
    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: DatabaseDefinition
    ): Boolean = true
}

/**
 * Description:
 */
class ModelViewValidator : Validator<ModelViewDefinition> {

    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: ModelViewDefinition
    ): Boolean = true
}

/**
 * Description: Validates to ensure a [OneToManyDefinition] is correctly coded. Will throw failures on the [ProcessorManager]
 */
class OneToManyValidator : Validator<OneToManyDefinition> {
    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: OneToManyDefinition
    ): Boolean = true
}

/**
 * Description: Validates proper usage of the [com.dbflow5.annotation.Table]
 */
class TableValidator : Validator<TableDefinition> {

    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: TableDefinition
    ): Boolean {
        var success = true

        if (!validatorDefinition.hasPrimaryConstructor) {
            processorManager.logError(
                TableValidator::class, "Table ${validatorDefinition.elementClassName}" +
                    " must provide a visible, parameterless constructor. Each field also must have a visible " +
                    "setter for now."
            )
            success = false
        }

        if (validatorDefinition.columnDefinitions.isEmpty()) {
            processorManager.logError(
                TableValidator::class, "Table ${validatorDefinition.associationalBehavior.name} " +
                    "of ${validatorDefinition.elementClassName}, ${validatorDefinition.element.javaClass} " +
                    "needs to define at least one column"
            )
            success = false
        }

        val hasTwoKinds = (validatorDefinition.primaryKeyColumnBehavior.hasAutoIncrement
            || validatorDefinition.primaryKeyColumnBehavior.hasRowID)
            && !validatorDefinition._primaryColumnDefinitions.isEmpty()

        if (hasTwoKinds) {
            processorManager.logError(
                TableValidator::class, "Table ${validatorDefinition.associationalBehavior.name}" +
                    " cannot mix and match autoincrement and composite primary keys"
            )
            success = false
        }

        val hasPrimary = (validatorDefinition.primaryKeyColumnBehavior.hasAutoIncrement
            || validatorDefinition.primaryKeyColumnBehavior.hasRowID)
            && validatorDefinition._primaryColumnDefinitions.isEmpty()
            || !validatorDefinition.primaryKeyColumnBehavior.hasAutoIncrement
            && !validatorDefinition.primaryKeyColumnBehavior.hasRowID
            && !validatorDefinition._primaryColumnDefinitions.isEmpty()
        if (!hasPrimary && validatorDefinition.type == TableDefinition.Type.Normal) {
            processorManager.logError(
                TableValidator::class, "Table ${validatorDefinition.associationalBehavior.name} " +
                    "needs to define at least one primary key"
            )
            success = false
        }

        return success
    }
}

class TypeConverterValidator : Validator<TypeConverterDefinition> {
    override fun validate(
        processorManager: ProcessorManager,
        validatorDefinition: TypeConverterDefinition
    ): Boolean {
        var success = true

        if (validatorDefinition.modelTypeName == null) {
            processorManager.logError(
                "TypeConverter: ${validatorDefinition.className} uses an " +
                    "unsupported Model Element parameter. If it has type parameters, you must " +
                    "remove them or subclass it for proper usage."
            )
            success = false
        } else if (validatorDefinition.dbTypeName == null) {
            processorManager.logError(
                "TypeConverter: ${validatorDefinition.className} uses an " +
                    "unsupported DB Element parameter. If it has type parameters, you must remove" +
                    " them or subclass it for proper usage."
            )
            success = false
        }

        return success
    }
}
