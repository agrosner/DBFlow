package com.dbflow5.processor.parser

import com.dbflow5.annotation.Fts4
import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.processor.utils.extractClassNameFromAnnotation
import com.squareup.kotlinpoet.javapoet.toKClassName

class Fts4Parser : Parser<Fts4, ClassModel.ClassType.Normal.Fts4> {

    override fun parse(input: Fts4): ClassModel.ClassType.Normal.Fts4 {
        return ClassModel.ClassType.Normal.Fts4(
            contentTable = input.extractClassNameFromAnnotation { it.contentTable }
                .toKClassName(),
        )
    }
}
