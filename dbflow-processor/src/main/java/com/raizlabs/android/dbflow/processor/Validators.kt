package com.raizlabs.android.dbflow.processor

import com.raizlabs.android.dbflow.processor.definition.*
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.EnumColumnAccessor
import com.raizlabs.android.dbflow.processor.definition.column.ReferenceColumnDefinition
import com.raizlabs.android.dbflow.processor.definition.column.PrivateScopeColumnAccessor
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty


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
    fun validate(processorManager: ProcessorManager, validatorDefinition: ValidatorDefinition): Boolean
}


/**
 * Description: Ensures the integrity of the annotation processor for columns.
 * @author Andrew Grosner (fuzz)
 */
class ColumnValidator : Validator<ColumnDefinition> {

    private var autoIncrementingPrimaryKey: ColumnDefinition? = null

    override fun validate(processorManager: ProcessorManager, validatorDefinition: ColumnDefinition): Boolean {

        var success = true

        // validate getter and setters.
        if (validatorDefinition.columnAccessor is PrivateScopeColumnAccessor) {
            val privateColumnAccess = validatorDefinition.columnAccessor as PrivateScopeColumnAccessor
            if (!validatorDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.getterNameElement)) {
                processorManager.logError(ColumnValidator::class,
                        "Could not find getter for private element: " + "\"%1s\" from table class: %1s. Consider adding a getter with name %1s or making it more accessible.",
                        validatorDefinition.elementName, validatorDefinition.baseTableDefinition.elementName,
                        privateColumnAccess.getterNameElement)
                success = false
            }
            if (!validatorDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.setterNameElement)) {
                processorManager.logError(ColumnValidator::class,
                        "Could not find setter for private element: \"${validatorDefinition.elementName}\"" +
                                " from table class: ${validatorDefinition.baseTableDefinition.elementName}. " +
                                "Consider adding a setter with name ${privateColumnAccess.setterNameElement} or making it more accessible.")
                success = false
            }
        }

        if (!validatorDefinition.defaultValue.isNullOrEmpty()) {
            val typeName = validatorDefinition.elementTypeName
            if (validatorDefinition is ReferenceColumnDefinition && validatorDefinition.isReferencingTableObject) {
                processorManager.logError(ColumnValidator::class, "Default values cannot be specified for model fields")
            } else if (typeName != null && typeName.isPrimitive) {
                processorManager.logWarning(ColumnValidator::class.java, "Primitive column types will not respect default values")
            }
        }

        if (validatorDefinition.columnName.isEmpty()) {
            success = false
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                    validatorDefinition.elementName, validatorDefinition.columnName,
                    validatorDefinition.elementTypeName)
        }

        if (validatorDefinition.columnAccessor is EnumColumnAccessor) {
            if (validatorDefinition.isPrimaryKey) {
                success = false
                processorManager.logError("Enums cannot be primary keys. Column: ${validatorDefinition.columnName}" +
                        " and type: ${validatorDefinition.elementTypeName}")
            } else if (validatorDefinition is ReferenceColumnDefinition) {
                success = false
                processorManager.logError("Enums cannot be foreign keys. Column: ${validatorDefinition.columnName}" +
                        " and type: ${validatorDefinition.elementTypeName}")
            }
        }

        if (validatorDefinition is ReferenceColumnDefinition) {
            validatorDefinition.column?.let {
                if (it.name.isNotEmpty()) {
                    success = false
                    processorManager.logError("Foreign Key ${validatorDefinition.elementName} cannot specify the column() field. "
                            + "Use a @ForeignKeyReference(columnName = {NAME} instead. " +
                            "Column: ${validatorDefinition.columnName} and type: ${validatorDefinition.elementTypeName}")
                }
            }

        } else {
            if (autoIncrementingPrimaryKey != null && validatorDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.")
                success = false
            }

            if (validatorDefinition.isPrimaryKeyAutoIncrement || validatorDefinition.isRowId) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = validatorDefinition
                } else if (autoIncrementingPrimaryKey != validatorDefinition) {
                    processorManager.logError("Only one auto-incrementing primary key is allowed on a table. " +
                            "Found Column: ${validatorDefinition.columnName} and type: ${validatorDefinition.elementTypeName}")
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
class ContentProviderValidator : Validator<ContentProviderDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          validatorDefinition: ContentProviderDefinition): Boolean {
        var success = true

        if (validatorDefinition.endpointDefinitions.isEmpty()) {
            processorManager.logError("The content provider ${validatorDefinition.element.simpleName} " +
                    "must have at least 1 @TableEndpoint associated with it")
            success = false
        }

        return success
    }
}

/**
 * Description:
 */
class DatabaseValidator : Validator<DatabaseDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          validatorDefinition: DatabaseDefinition): Boolean = true
}

/**
 * Description:
 */
class ModelViewValidator : Validator<ModelViewDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: ModelViewDefinition): Boolean = true
}

/**
 * Description: Validates to ensure a [OneToManyDefinition] is correctly coded. Will throw failures on the [ProcessorManager]
 */
class OneToManyValidator : Validator<OneToManyDefinition> {
    override fun validate(processorManager: ProcessorManager, validatorDefinition: OneToManyDefinition): Boolean = true
}

class TableEndpointValidator : Validator<TableEndpointDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: TableEndpointDefinition): Boolean {
        var success = true

        if (validatorDefinition.contentUriDefinitions.isEmpty()) {
            processorManager.logError("A table endpoint ${validatorDefinition.elementClassName} " +
                    "must supply at least one @ContentUri")
            success = false
        }

        return success
    }
}

/**
 * Description: Validates proper usage of the [com.raizlabs.android.dbflow.annotation.Table]
 */
class TableValidator : Validator<TableDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: TableDefinition): Boolean {
        var success = true

        if (!validatorDefinition.hasPrimaryConstructor) {
            processorManager.logError(TableValidator::class, "Table ${validatorDefinition.elementClassName}" +
                    " must provide a visible, default constructor.")
            success = false
        }

        if (validatorDefinition.columnDefinitions.isEmpty()) {
            processorManager.logError(TableValidator::class, "Table ${validatorDefinition.tableName} " +
                    "of ${validatorDefinition.elementClassName}, ${validatorDefinition.element.javaClass} " +
                    "needs to define at least one column")
            success = false
        }

        val hasTwoKinds = (validatorDefinition.hasAutoIncrement || validatorDefinition.hasRowID)
                && !validatorDefinition._primaryColumnDefinitions.isEmpty()

        if (hasTwoKinds) {
            processorManager.logError(TableValidator::class, "Table ${validatorDefinition.tableName}" +
                    " cannot mix and match autoincrement and composite primary keys")
            success = false
        }

        val hasPrimary = (validatorDefinition.hasAutoIncrement || validatorDefinition.hasRowID)
                && validatorDefinition._primaryColumnDefinitions.isEmpty()
                || !validatorDefinition.hasAutoIncrement && !validatorDefinition.hasRowID
                && !validatorDefinition._primaryColumnDefinitions.isEmpty()

        if (!hasPrimary) {
            processorManager.logError(TableValidator::class, "Table ${validatorDefinition.tableName} " +
                    "needs to define at least one primary key")
            success = false
        }

        return success
    }
}

class TypeConverterValidator : Validator<TypeConverterDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          validatorDefinition: TypeConverterDefinition): Boolean {
        var success = true

        if (validatorDefinition.modelTypeName == null) {
            processorManager.logError("TypeConverter: ${validatorDefinition.className} uses an " +
                    "unsupported Model Element parameter. If it has type parameters, you must " +
                    "remove them or subclass it for proper usage.")
            success = false
        } else if (validatorDefinition.dbTypeName == null) {
            processorManager.logError("TypeConverter: ${validatorDefinition.className} uses an " +
                    "unsupported DB Element parameter. If it has type parameters, you must remove" +
                    " them or subclass it for proper usage.")
            success = false
        }

        return success
    }
}
