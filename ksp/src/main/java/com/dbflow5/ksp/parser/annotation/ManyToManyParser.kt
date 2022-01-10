package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.model.interop.KSPClassType
import com.dbflow5.ksp.model.interop.KSPOriginatingFile
import com.dbflow5.ksp.parser.Parser
import com.dbflow5.ksp.parser.validation.ValidationException
import com.dbflow5.model.ManyToManyModel
import com.dbflow5.model.NameModel
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
class ManyToManyParser(
    private val manyToManyPropertyParser: ManyToManyPropertyParser,
) : Parser<ManyToManyParser.Input, ManyToManyModel> {

    data class Input(
        val annotation: KSAnnotation,
        val name: NameModel,
        val classType: ClassName,
        val databaseTypeName: TypeName,
        val ksClassDeclaration: KSClassDeclaration,
        val originatingFile: KSPOriginatingFile,
    )

    @Throws(ValidationException::class)
    override fun parse(input: Input): ManyToManyModel {
        return ManyToManyModel(
            name = input.name,
            properties = manyToManyPropertyParser.parse(input.annotation),
            classType = input.classType,
            databaseTypeName = input.databaseTypeName,
            ksType = KSPClassType(input.ksClassDeclaration.asStarProjectedType()),
            originatingFile = input.originatingFile,
        )
    }
}