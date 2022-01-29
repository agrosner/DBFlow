package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ObjectModel
import kotlin.reflect.KClass

/**
 * Description: Holds all validations
 * enabling validation lookup by object type.
 */
class ObjectValidatorMap(
    private val validators: Map<KClass<out ObjectModel>, Validator<out ObjectModel>>,
) : Validator<ObjectModel> {

    @Suppress("unchecked_cast")
    @Throws(ValidationException::class)
    override fun validate(value: ObjectModel) {
        validators[value::class]?.let { validator ->
            (validator as GroupedValidator<ObjectModel>).validate(value)
        }
    }
}
