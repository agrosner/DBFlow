package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description: Validates proper usage of the [com.raizlabs.android.dbflow.annotation.Table]
 */
class TableValidator : Validator<TableDefinition> {

    override fun validate(processorManager: ProcessorManager, validatorDefinition: TableDefinition): Boolean {
        var success = true

        if (validatorDefinition.columnDefinitions.isEmpty()) {
            processorManager.logError(TableValidator::class, "Table %1s of %1s, %1s needs to define at least one column", validatorDefinition.tableName,
                    validatorDefinition.elementClassName, validatorDefinition.element.javaClass)
            success = false
        }

        val hasTwoKinds = (validatorDefinition.hasAutoIncrement || validatorDefinition.hasRowID) && !validatorDefinition.primaryColumnDefinitions.isEmpty()

        if (hasTwoKinds) {
            processorManager.logError(TableValidator::class, "Table %1s cannot mix and match autoincrement and composite primary keys",
                    validatorDefinition.tableName)
            success = false
        }

        val hasPrimary = (validatorDefinition.hasAutoIncrement || validatorDefinition.hasRowID) && validatorDefinition.primaryColumnDefinitions.isEmpty()
                || !validatorDefinition.hasAutoIncrement && !validatorDefinition.hasRowID && !validatorDefinition.primaryColumnDefinitions.isEmpty()

        if (!hasPrimary) {
            processorManager.logError(TableValidator::class, "Table %1s needs to define at least one primary key", validatorDefinition.tableName)
            success = false
        }

        return success
    }
}
