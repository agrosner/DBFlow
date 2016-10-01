package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.column.*
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.StringUtils

/**
 * Description: Ensures the integrity of the annotation processor for columns.
 */
class ColumnValidator : Validator<ColumnDefinition> {

    private var autoIncrementingPrimaryKey: ColumnDefinition? = null

    override fun validate(processorManager: ProcessorManager, columnDefinition: ColumnDefinition): Boolean {

        var success = true

        // validate getter and setters.
        if (columnDefinition.columnAccess is PrivateColumnAccess || columnDefinition.columnAccess is WrapperColumnAccess && (columnDefinition.columnAccess as WrapperColumnAccess).existingColumnAccess is PrivateColumnAccess) {
            val privateColumnAccess = if (columnDefinition.columnAccess is PrivateColumnAccess)
                columnDefinition.columnAccess as PrivateColumnAccess
            else
                (columnDefinition.columnAccess as WrapperColumnAccess).existingColumnAccess as PrivateColumnAccess
            if (!columnDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.getGetterNameElement(columnDefinition.elementName))) {
                processorManager.logError(ColumnValidator::class.java, "Could not find getter for private element: " + "\"%1s\" from table class: %1s. Consider adding a getter with name %1s or making it more accessible.",
                        columnDefinition.elementName, columnDefinition.baseTableDefinition.elementName, privateColumnAccess.getGetterNameElement(columnDefinition.elementName))
                success = false
            }
            if (!columnDefinition.baseTableDefinition.classElementLookUpMap.containsKey(privateColumnAccess.getSetterNameElement(columnDefinition.elementName))) {
                processorManager.logError(ColumnValidator::class.java, "Could not find setter for private element: " + "\"%1s\" from table class: %1s. Consider adding a setter with name %1s or making it more accessible.",
                        columnDefinition.elementName, columnDefinition.baseTableDefinition.elementName, privateColumnAccess.getSetterNameElement(columnDefinition.elementName))
                success = false
            }
        }

        if (!StringUtils.isNullOrEmpty(columnDefinition.defaultValue)) {
            if (columnDefinition is ForeignKeyColumnDefinition && columnDefinition.isModel) {
                processorManager.logError(ColumnValidator::class.java, "Default values cannot be specified for model fields")
            } else if (columnDefinition.elementTypeName.isPrimitive) {
                processorManager.logWarning(ColumnValidator::class.java, "Primitive column types will not respect default values")
            }
        }

        if (columnDefinition.columnName.isEmpty()) {
            success = false
            processorManager.logError("Field %1s cannot have a null column name for column: %1s and type: %1s",
                    columnDefinition.elementName, columnDefinition.columnName,
                    columnDefinition.elementTypeName)
        }

        if (columnDefinition.columnAccess is EnumColumnAccess) {
            if (columnDefinition.isPrimaryKey) {
                success = false
                processorManager.logError("Enums cannot be primary keys. Column: %1s and type: %1s", columnDefinition.columnName,
                        columnDefinition.elementTypeName)
            } else if (columnDefinition is ForeignKeyColumnDefinition) {
                success = false
                processorManager.logError("Enums cannot be foreign keys. Column: %1s and type: %1s", columnDefinition.columnName,
                        columnDefinition.elementTypeName)
            }
        }

        if (columnDefinition is ForeignKeyColumnDefinition) {
            columnDefinition.column?.let {
                if (it.name.length > 0) {
                    success = false
                    processorManager.logError("Foreign Key %1s cannot specify the column() field. "
                            + "Use a @ForeignKeyReference(columnName = {NAME} instead. Column: %1s and type: %1s",
                            columnDefinition.elementName, columnDefinition.columnName,
                            columnDefinition.elementTypeName)
                }
            }

        } else {
            if (autoIncrementingPrimaryKey != null && columnDefinition.isPrimaryKey) {
                processorManager.logError("You cannot mix and match autoincrementing and composite primary keys.")
                success = false
            }

            if (columnDefinition.isPrimaryKeyAutoIncrement || columnDefinition.isRowId) {
                if (autoIncrementingPrimaryKey == null) {
                    autoIncrementingPrimaryKey = columnDefinition
                } else if (autoIncrementingPrimaryKey != columnDefinition) {
                    processorManager.logError(
                            "Only one autoincrementing primary key is allowed on a table. Found Column: %1s and type: %1s",
                            columnDefinition.columnName, columnDefinition.elementTypeName)
                    success = false
                }
            }
        }

        return success
    }
}
