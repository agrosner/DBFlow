package com.dbflow5.ksp.parser.annotation

import com.dbflow5.ksp.model.ManyToManyModel
import com.dbflow5.ksp.model.NameModel
import com.dbflow5.ksp.parser.Parser
import com.dbflow5.ksp.parser.arg
import com.dbflow5.ksp.parser.mapProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
class MultipleManyToManyParser(
    private val manyToManyParser: ManyToManyParser,
) : Parser<MultipleManyToManyParser.Input, List<ManyToManyModel>> {
    data class Input(
        val annotation: KSAnnotation,
        val name: NameModel,
        val classType: ClassName,
        val databaseTypeName: TypeName,
        val ksClassDeclaration: KSClassDeclaration,
        val originatingFile: KSFile?,
    )

    override fun parse(input: Input): List<ManyToManyModel> {
        return input.annotation.arguments.mapProperties().run {
            arg<List<KSAnnotation>>("value").map { annotation ->
                manyToManyParser.parse(
                    ManyToManyParser.Input(
                        annotation = annotation,
                        name = input.name,
                        classType = input.classType,
                        databaseTypeName = input.databaseTypeName,
                        ksClassDeclaration = input.ksClassDeclaration,
                        originatingFile = input.originatingFile,
                    )
                )
            }
        }
    }
}
