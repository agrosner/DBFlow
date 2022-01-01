package com.dbflow5.ksp.parser

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.jvm.Throws

class Fts4Parser : Parser<KSAnnotation, ClassModel.ClassType.Normal.Fts4> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): ClassModel.ClassType.Normal.Fts4 {
        val args = input.arguments.mapProperties()
        return ClassModel.ClassType.Normal.Fts4(
            contentTable = args.typeName("contentTable"),
        )
    }
}