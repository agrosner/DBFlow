package com.dbflow5.processor.parser

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.OneToManyRelation
import com.dbflow5.annotation.Table
import com.dbflow5.annotation.TypeConverter
import com.dbflow5.codegen.model.DatabaseModel
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.ObjectModel
import com.dbflow5.codegen.model.OneToManyModel
import com.dbflow5.codegen.model.TypeConverterModel
import com.dbflow5.codegen.model.cache.extractTypeConverter
import com.dbflow5.codegen.parser.Parser
import com.dbflow5.processor.interop.KaptClassDeclaration
import com.dbflow5.processor.interop.KaptClassType
import com.dbflow5.processor.interop.KaptOriginatingFileType
import com.dbflow5.processor.interop.invoke
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.asStarProjectedType
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toTypeErasedElement
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Description:
 */
class KaptElementProcessor(
    private val elements: Elements,
    private val databasePropertyParser: DatabasePropertyParser,
    private val typeConverterPropertyParser: TypeConverterPropertyParser,
    private val oneToManyPropertyParser: OneToManyPropertyParser,
    private val tablePropertyParser: TablePropertyParser,
) : Parser<TypeElement,
    List<ObjectModel>> {

    override fun parse(input: TypeElement): List<ObjectModel> {
        val annotations = elements.getAllAnnotationMirrors(input)
        val name = NameModel(
            input.simpleName,
            input.getPackage(),
        )
        val classType = input.toTypeErasedElement()!!.asClassName()
        return annotations.mapNotNull { annotation ->
            val typeName = annotation.annotationType.asTypeName()
            when (typeName) {
                typeNameOf<Database>() -> {
                    // TODO: check super type
                    listOf(
                        DatabaseModel(
                            name = name,
                            classType = classType,
                            properties = databasePropertyParser.parse(input.annotation()!!),
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                typeNameOf<TypeConverter>() -> {
                    val typeConverterSuper = extractTypeConverter(
                        KaptClassDeclaration(input),
                        classType
                    )
                    listOf(
                        TypeConverterModel.Simple(
                            name = name,
                            properties = typeConverterPropertyParser.parse(input.annotation()),
                            classType = classType,
                            dataTypeName = typeConverterSuper.typeArguments[0],
                            modelTypeName = typeConverterSuper.typeArguments[1],
                            modelClass = null,
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                typeNameOf<ManyToMany>() -> {
                    listOf() // TODO: many to many
                }
                typeNameOf<OneToManyRelation>() ->{
                    val tableProperties = tablePropertyParser.parse(
                        input.annotation()
                    )
                    listOf(
                        OneToManyModel(
                            name = name,
                            properties = oneToManyPropertyParser.parse(input.annotation()),
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            ksType =KaptClassType(input.asStarProjectedType()),
                            originatingFile = KaptOriginatingFileType,
                        )
                    )
                }
                else -> null
            }
        }.flatten()
    }
}