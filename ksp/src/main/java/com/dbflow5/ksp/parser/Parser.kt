package com.dbflow5.ksp.parser

import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation

/**
 * Default handling converting annotation args to [Out].
 */
interface AnnotationParser<Out> : Parser<KSAnnotation, Out> {

    override fun parse(input: KSAnnotation): Out = input.arguments.mapProperties().parse()

    @Throws(ValidationException::class)
    fun ArgMap.parse(): Out
}
