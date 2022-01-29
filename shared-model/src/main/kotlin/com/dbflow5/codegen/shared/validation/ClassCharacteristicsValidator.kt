package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel

/**
 * Description: Validates class is not abstract, or object, etc.
 */
class ClassCharacteristicsValidator : Validator<ClassModel> {

    @Throws(ValidationException::class)
    override fun validate(value: ClassModel) {
        val declaration = value.ksClassType.declaration
        declaration.closestClassDeclaration?.let { classDeclaration ->
            if (classDeclaration.isObject) {
                throw ValidationException(OBJECT_MSG, value.name)
            } else if (classDeclaration.isEnum) {
                throw ValidationException(ENUM_MSG, value.name)
            }
        }
        if (declaration.isAbstract) {
            throw ValidationException(ABSTRACT_MSG, value.name)
        }
    }

    companion object {
        const val OBJECT_MSG = "Objects cannot be DB objects."
        const val ENUM_MSG = "Enums cannot be DB objects."
        const val ABSTRACT_MSG = "Abstract classes cannot be DB objects."
    }
}