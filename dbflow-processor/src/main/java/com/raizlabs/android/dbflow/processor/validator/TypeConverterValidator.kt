package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description:
 */
class TypeConverterValidator : Validator<TypeConverterDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          validatorDefinition: TypeConverterDefinition): Boolean {
        var success = true

        if (validatorDefinition.modelTypeName == null) {
            processorManager.logError("TypeConverter: " + validatorDefinition.className.toString() +
                    " uses an unsupported Model Element parameter. If it has type parameters, you must remove them or subclass it" +
                    "for proper usage.")
            success = false
        } else if (validatorDefinition.dbTypeName == null) {
            processorManager.logError("TypeConverter: " + validatorDefinition.className.toString() +
                    " uses an unsupported DB Element parameter. If it has type parameters, you must remove them or subclass it " +
                    "for proper usage.")
            success = false
        }

        return success
    }
}
