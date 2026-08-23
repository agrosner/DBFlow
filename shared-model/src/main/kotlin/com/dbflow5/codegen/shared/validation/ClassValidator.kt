package com.dbflow5.codegen.shared.validation

import com.dbflow5.codegen.shared.ClassModel

class ClassValidator(
    fieldValidator: FieldValidator,
) : GroupedValidator<ClassModel>(
    listOf(
        // order matters. checks top-level first for clarity
        ClassCharacteristicsValidator(),
        PrimaryValidator(),
        ClassToFieldValidator(fieldValidator),
    )
)
