package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description: Validates proper usage of the [com.raizlabs.android.dbflow.annotation.Table]
 */
class TableValidator : Validator<TableDefinition> {

    override fun validate(processorManager: ProcessorManager, tableDefinition: TableDefinition): Boolean {
        var success = true

        if (tableDefinition.columnDefinitions.isEmpty()) {
            processorManager.logError(TableValidator::class.java, "Table %1s of %1s, %1s needs to define at least one column", tableDefinition.tableName,
                    tableDefinition.elementClassName, tableDefinition.element.javaClass)
            success = false
        }

        val hasTwoKinds = (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) && !tableDefinition.primaryColumnDefinitions.isEmpty()

        if (hasTwoKinds) {
            processorManager.logError(TableValidator::class.java, "Table %1s cannot mix and match autoincrement and composite primary keys",
                    tableDefinition.tableName)
            success = false
        }

        val hasPrimary = (tableDefinition.hasAutoIncrement || tableDefinition.hasRowID) && tableDefinition.primaryColumnDefinitions.isEmpty()
                || !tableDefinition.hasAutoIncrement && !tableDefinition.hasRowID && !tableDefinition.primaryColumnDefinitions.isEmpty()

        if (!hasPrimary) {
            processorManager.logError(TableValidator::class.java, "Table %1s needs to define at least one primary key", tableDefinition.tableName)
            success = false
        }

        return success
    }
}
