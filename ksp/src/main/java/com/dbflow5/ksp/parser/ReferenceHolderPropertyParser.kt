package com.dbflow5.ksp.parser

import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.ksp.model.properties.ReferenceHolderProperties
import com.dbflow5.ksp.parser.validation.ValidationException
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.asTypeName
import kotlin.jvm.Throws

class ReferenceHolderPropertyParser
constructor(
    private val foreignKeyReferencePropertyParser: ForeignKeyReferencePropertyParser,
) : Parser<KSAnnotation, ReferenceHolderProperties> {
    @Throws(ValidationException::class)
    override fun parse(input: KSAnnotation): ReferenceHolderProperties {
        val args = input.arguments.mapProperties()
        val references = args.arg<List<KSAnnotation>>("references")
            .map { foreignKeyReferencePropertyParser.parse(it) }
        return ReferenceHolderProperties(
            onDelete = args.ifArg("onDelete") {
                enumArg(it, ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            onUpdate = args.ifArg("onUpdate") {
                enumArg("onUpdate", ForeignKeyAction::valueOf)
            } ?: ForeignKeyAction.NO_ACTION,
            referencesType = when (references.isNotEmpty()) {
                true -> ReferenceHolderProperties.ReferencesType.Specific(
                    references,
                )
                else -> ReferenceHolderProperties.ReferencesType.All
            },
            referencedTableTypeName = args.ifArg("tableClass") {
                className("tableClass")
            } ?: Any::class.asTypeName()
        )
    }
}