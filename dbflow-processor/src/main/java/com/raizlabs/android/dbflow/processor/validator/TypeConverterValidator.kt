package com.raizlabs.android.dbflow.processor.validator

import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager

/**
 * Description:
 */
class TypeConverterValidator : Validator<TypeConverterDefinition> {
    override fun validate(processorManager: ProcessorManager,
                          typeConverterDefinition: TypeConverterDefinition): Boolean {
        var success = true

        if (typeConverterDefinition.modelTypeName == null) {
            processorManager.logError("TypeConverter: " + typeConverterDefinition.className.toString() +
                    " uses an unsupported Model Element parameter. If it has type parameters, you must remove them or subclass it" +
                    "for proper usage.")
            success = false
        } else if (typeConverterDefinition.dbTypeName == null) {
            processorManager.logError("TypeConverter: " + typeConverterDefinition.className.toString() +
                    " uses an unsupported DB Element parameter. If it has type parameters, you must remove them or subclass it " +
                    "for proper usage.")
            success = false
        }

        return success
    }
}
