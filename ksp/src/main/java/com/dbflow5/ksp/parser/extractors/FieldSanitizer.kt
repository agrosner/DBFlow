package com.dbflow5.ksp.parser.extractors

import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.annotation.OneToMany
import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.parser.KSPropertyDeclarationParser
import com.dbflow5.ksp.parser.Parser
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Description: Extracts valid field types.
 */
class FieldSanitizer(
    private val propertyParser: KSPropertyDeclarationParser,
) : Parser<Sequence<KSPropertyDeclaration>,
    List<FieldModel>> {

    override fun parse(input: Sequence<KSPropertyDeclaration>) =
        input.filterNot { prop ->
            isIgnoredColumn(prop) || isModelAdapter(prop)
                || isOneToMany(prop)
        }
            .map(propertyParser::parse)
            .toList()

    private fun isModelAdapter(prop: KSPropertyDeclaration) =
        prop.type.toTypeName() == ClassNames.modelAdapter(ClassNames.BaseModel)

    private fun isIgnoredColumn(prop: KSPropertyDeclaration) =
        checkAllPropAnnotations(prop) {
            it.annotationType.toTypeName() == typeNameOf<ColumnIgnore>()
        }

    private fun isOneToMany(prop: KSPropertyDeclaration): Boolean = checkAllPropAnnotations(prop) {
        it.annotationType.toTypeName() == typeNameOf<OneToMany>()
    }

    private fun checkAllPropAnnotations(
        prop: KSPropertyDeclaration,
        predicate: (KSAnnotation) -> Boolean
    ) =
        prop.annotations.any(predicate)
            || prop.getter?.annotations?.any(predicate) == true
            || prop.setter?.annotations?.any(predicate) == true
}