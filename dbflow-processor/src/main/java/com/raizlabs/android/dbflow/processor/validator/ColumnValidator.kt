package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.StringUtils

/**
 * Description: Ensures the integrity of the annotation processor for columns.
 */
class ColumnValidator : Validator<ColumnDefinition> {

    private var autoIncrementingPrimaryKey: ColumnDefinition? = null

    override fun validate(processorManager: ProcessorManager, validatorDefinition: ColumnDefinition): Boolean {

        var success = true

        // validate getter and setters.
        if (validatorDefinition.columnAccess is PrivateColumnAccess || validatorDefinition.columnAccess is WrapperColumnAccess && (validatorDefinition.columnAccess as WrapperColumnAccess).existingColumnAccess is PrivateColumnAccess) {
            val privateColumnAccess = if (validatorDefinition.columnAccess is PrivateColumnAccess)
                validatorDefinition.columnAccess as PrivateColumnAccess
            else
                (validatorDefinition.columnAccess as WrapperColumnAccess).existingColumnAccess as PrivateColumnAccess
            if (!validatorDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.getGetterNameElement(validatorDefinition.elementName))) {
                processorManager.logError(ColumnValidator::class.java, "Could not find getter for private element: " + "\"%1s\" from table class: %1s. Consider adding a getter with name %1s or making it more accessible.",
                        validatorDefinition.elementName, validatorDefinition.baseTableDefinition.elementName, privateColumnAccess.getGetterNameElement(validatorDefinition.elementName))
                success = false
            }
            if (!validatorDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.getSetterNameElement(validatorDefinition.elementName))) {
                processorManager.logError(ColumnValidator::class.java, "Could not find setter for private element: " + "\"%1s\" from table class: %1s. Consider adding a setter with name %1s or making it more accessible.",
                        validatorDefinition.elementName, validatorDefinition.baseTableDefinition.elementName, privateColumnAccess.getSetterNameElement(validatorDefinition.elementName))
                success = false
            }
        }

        if (!StringUtils.isNullOrEmpty(validatorDefinition.defaultValue)) {
            if (validatorDefinition is ForeignKeyColumnDefinition && validatorDefinition.isModel) {
                processorManager.logError(ColumnValidator::class.java, "Default values cannot be specified for model fields")
            } else if (validatorDefinition.elementTypeName.isPrimitive) {
                processorManager.logWarning(ColumnValidator::class.java, "Primitive column types will not respect default values")
            }
        }

        if (validatorDefinition.columnName.isEmpty()) {
            success = false
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                    validatorDefinition.elementName, validatorDefinition.columnName,
                    validatorDefinition.elementTypeName)
        }

        if (validatorDefinition.columnAccess is EnumColumnAccess) {
            if (validatorDefinition.isPrimaryKey) {
                success = false
                processorManager.logError("Enums cannot be primary keys. Column: %1s and type: %1s", validatorDefinition.columnName,
                        validatorDefinition.elementTypeName)
            } else if (validatorDefinition is ForeignKeyColumnDefinition) {
                success = false
                processorManager.logError("Enums cannot be foreign keys. Column: %1s and type: %1s", validatorDefinition.columnName,
                        validatorDefinition.elementTypeName)
            }
        }

        if (validatorDefinition is ForeignKeyColumnDefinition) {
            validatorDefinition.column?.let {
                if (it.name.length > 0) {
                    success = false
                    processorManager.logError("Foreign Key %1s cannot specify the column() field. "
                            + "Use a @ForeignKeyReference(columnName = {NAME} instead. Column: %1s and type: %1s",
                            validatorDefinition.elementName, validatorDefinition.columnName,
                            validatorDefinition.elementTypeName)
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
                    processorManager.logError(
                            "Only one autoincrementing primary key is allowed on a table. Found Column: %1s and type: %1s",
                            validatorDefinition.columnName, validatorDefinition.elementTypeName)
                    success = false
                }
            }
        }

        return success
    }
}
