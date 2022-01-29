package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.TypeConverterModel

/**
 * Description:
 */
class TypeConverterValidator : Validator<TypeConverterModel> {

    @Throws(ValidationException::class)
    override fun validate(value: TypeConverterModel) {
        if (value.modelTypeName == value.dataTypeName) {
            throw ValidationException(
                SAME_TYPE_MSG, value.name,
            )
        }

    }

    companion object {
        const val SAME_TYPE_MSG = "TypeConverters cannot have the same input output type."
    }
}