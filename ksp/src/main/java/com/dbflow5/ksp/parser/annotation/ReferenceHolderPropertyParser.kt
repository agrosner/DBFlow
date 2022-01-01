package com.dbflow5.ksp.parser.annotation

import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.parser.AnnotationParser
import com.dbflow5.ksp.parser.ArgMap
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.className
import com.dbflow5.ksp.parser.enumArg
import com.dbflow5.ksp.parser.ifArg
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asTypeName

class ReferenceHolderPropertyParser
constructor(
    private val foreignKeyReferencePropertyParser: ForeignKeyReferencePropertyParser,
) : AnnotationParser<ReferenceHolderProperties> {
    @Throws(ValidationException::class)
    override fun ArgMap.parse(): ReferenceHolderProperties {
        val references = arg<List<KSAnnotation>>("references")
            .map { foreignKeyReferencePropertyParser.parse(it) }
        return ReferenceHolderProperties(
            onDelete = ifArg("onDelete") {
                enumArg(it, ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            onUpdate = ifArg("onUpdate") {
                enumArg("onUpdate", ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            referencesType = when (references.isNotEmpty()) {
                true -> ReferenceHolderProperties.ReferencesType.Specific(
                    references,
                )
                else -> ReferenceHolderProperties.ReferencesType.All
            },
            referencedTableTypeName = ifArg("tableClass") {
                className("tableClass")
            } ?: Any::class.asTypeName()
        )
    }
}