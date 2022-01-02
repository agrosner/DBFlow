package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.typeName
import com.dbflow5.ksp.parser.validation.ValidationException

class Fts4Parser : AnnotationParser<ClassModel.ClassType.Normal.Fts4> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ClassModel.ClassType.Normal.Fts4 {
        return ClassModel.ClassType.Normal.Fts4(
            contentTable = typeName("contentTable"),
        )
    }
}
