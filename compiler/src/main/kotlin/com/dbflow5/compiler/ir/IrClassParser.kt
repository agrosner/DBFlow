package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.ClassAdapterFieldModel
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.DatabaseModel
import com.dbflow5.codegen.shared.ManyToManyModel
import com.dbflow5.codegen.shared.MigrationModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.ObjectModel
import com.dbflow5.codegen.shared.OneToManyModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.extractTypeConverter
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.parser.Parser
import com.dbflow5.codegen.shared.properties.ModelViewQueryProperties
import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.codegen.shared.validation.ValidationExceptionProvider
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties

internal class IrClassParser(
    private val fieldSanitizer: FieldSanitizer,
) : Parser<IrClass, List<ObjectModel>> {

    sealed interface Validation : ValidationExceptionProvider {
        data class QualifiedName(val className: ClassName) : Validation {
            override val message: String = "Missing Qualified name for class $className"
        }

        data class MissingTable(
            val annotationName: String,
            val className: ClassName,
        ) : Validation {
            override val message: String =
                "Missing @Table: $className must also declare a Table annotation with $annotationName"
        }

        data class InvalidConstructor(
            val className: ClassName,
            val fieldsCount: Int,
            val constructorCount: Int? = null,
        ) : Validation {
            override val message: String =
                "Invalid constructor: Could not find a valid constructor to create the model " +
                    "$className. Ensure either all properties are in constructor, or a single, default " +
                    "constructor exists. Found ($fieldsCount) fields" +
                    if (constructorCount != null) " for ($constructorCount) constructor fields." else "."
        }

        data class InvalidSuperType(
            val className: ClassName,
            val expectedSuperType: ClassName,
        ) : Validation {
            override val message: String =
                "Invalid supertype: Expected $expectedSuperType for $className."
        }
    }

    override fun parse(input: IrClass): List<ObjectModel> {
        val fqName = input.fqNameWhenAvailable
            ?: throw Validation.QualifiedName(ClassName("", input.name.asString())).exception
        val classType = input.toPoetClassName()
        val name = NameModel(classType)
        val originating = IrOriginatingSource(fqName.asString())
        return buildList {
            input.annotation(IrTypeFqNames.Database)?.let { annotation ->
                if (!input.superTypes.any { type ->
                        val poet = type.toPoetTypeName()
                        val raw = (poet as? ParameterizedTypeName)?.rawType ?: poet
                        raw == ClassNames.DBFlowDatabase
                    }
                ) {
                    throw Validation.InvalidSuperType(classType, ClassNames.DBFlowDatabase).exception
                }
                add(
                    DatabaseModel(
                        name = name,
                        classType = classType,
                        properties = annotation.toAnnotationMap().toDatabaseProperties(),
                        originatingSource = originating,
                        adapterFields = input.properties
                            .filter { it.isAbstract }
                            .mapNotNull { property ->
                                val typeName = (property.getter?.returnType ?: property.backingField?.type)
                                    ?.toPoetTypeName() as? ParameterizedTypeName
                                    ?: return@mapNotNull null
                                if (typeName.rawType !in ClassAdapterFieldModel.Type.entries.map { it.className }) {
                                    return@mapNotNull null
                                }
                                ClassAdapterFieldModel(
                                    NameModel(
                                        packageName = input.poetPackageName(),
                                        shortName = property.name.asString(),
                                        nullable = typeName.isNullable,
                                    ),
                                    typeName = typeName,
                                )
                            }
                            .toList(),
                    )
                )
            }
            input.annotation(IrTypeFqNames.TypeConverter)?.let { annotation ->
                val typeConverterSuper = extractTypeConverter(IrClassDeclaration(input), classType)
                add(
                    TypeConverterModel.Simple(
                        name = name,
                        properties = annotation.toAnnotationMap().toTypeConverterProperties(),
                        classType = classType,
                        dataTypeName = typeConverterSuper.typeArguments[0],
                        modelTypeName = typeConverterSuper.typeArguments[1],
                        modelClass = null,
                        originatingSource = originating,
                    )
                )
            }
            input.annotation(IrTypeFqNames.ManyToMany)?.let { annotation ->
                add(manyToManyModel(input, classType, name, annotation, originating))
            }
            input.annotation(IrTypeFqNames.MultipleManyToMany)?.let { annotation ->
                val table = input.annotation(IrTypeFqNames.Table)
                    ?: throw Validation.MissingTable("ManyToMany", classType).exception
                val tableProperties = table.toAnnotationMap().toTableProperties()
                annotation.toAnnotationMap().nestedList("value").forEach { nested ->
                    add(
                        ManyToManyModel(
                            name = name,
                            properties = nested.toManyToManyProperties(),
                            classType = classType,
                            databaseTypeName = tableProperties.database,
                            ksType = IrClassType(input.defaultType),
                            originatingSource = originating,
                        )
                    )
                }
            }
            input.annotation(IrTypeFqNames.OneToMany)?.let { annotation ->
                val table = input.annotation(IrTypeFqNames.Table)
                    ?: throw Validation.MissingTable("OneToManyRelation", classType).exception
                add(
                    OneToManyModel(
                        name = name,
                        properties = annotation.toAnnotationMap().toOneToManyProperties(),
                        classType = classType,
                        databaseTypeName = table.toAnnotationMap().toTableProperties().database,
                        ksType = IrClassType(input.defaultType),
                        originatingSource = originating,
                    )
                )
            }
            input.annotation(IrTypeFqNames.Migration)?.let { annotation ->
                if (input.constructors.none { ctor ->
                        ctor.regularParameters.isEmpty() ||
                            ctor.regularParameters.all { it.hasDefaultValue() }
                    }
                ) {
                    throw ValidationException("Migration classes must contain only empty constructor.")
                }
                add(
                    MigrationModel(
                        name = name,
                        properties = annotation.toAnnotationMap().toMigrationProperties(),
                        classType = classType,
                        originatingSource = originating,
                    )
                )
            }
            addAll(handleClass(input, classType, name, originating))
        }
    }

    private fun handleClass(
        input: IrClass,
        classType: ClassName,
        name: NameModel,
        originatingSource: IrOriginatingSource,
    ): List<ObjectModel> {
        val table = input.annotation(IrTypeFqNames.Table)
        val view = input.annotation(IrTypeFqNames.ModelView)
        val query = input.annotation(IrTypeFqNames.Query)
        if (table == null && view == null && query == null) return emptyList()

        val emptyConstructor = findDefaultConstructor(input)
        val hasDefaultConstructor = emptyConstructor != null
        val declaration = IrClassDeclaration(input)
        val fields = fieldSanitizer.parse(declaration)
        if (emptyConstructor == null) {
            validateMatchingConstructor(input, classType, fields)
        }
        val type = IrClassType(input.defaultType)
        val implementsLoadFromCursorListener = declaration.superTypes.any {
            it == ClassNames.LoadFromCursorListener
        }
        val implementsSQLiteStatementListener = declaration.superTypes.any {
            it == ClassNames.DatabaseStatementListener
        }
        val granularNotifications = input.hasAnnotation(IrTypeFqNames.GranularNotifications)
        return when {
            table != null -> {
                val properties = table.toAnnotationMap().toTableProperties()
                val fts3 = input.hasAnnotation(IrTypeFqNames.Fts3)
                val fts4 = input.annotation(IrTypeFqNames.Fts4)
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        ksClassType = type,
                        isDataClass = declaration.isData,
                        type = when {
                            fts3 -> ClassModel.Type.Table.Fts3
                            fts4 != null -> fts4.toAnnotationMap().toFts4Type()
                            else -> ClassModel.Type.Table.Normal
                        },
                        properties = properties,
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = declaration.isInternal,
                        granularNotifications = granularNotifications,
                        originatingSource = originatingSource,
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        indexGroups = properties.indexGroupProperties
                            .map { group -> group.toModel(classType, fields) },
                        uniqueGroups = properties.uniqueGroupProperties
                            .map { group -> group.toModel(fields) },
                    )
                )
            }
            view != null -> {
                val args = view.toAnnotationMap()
                listOf(
                    ClassModel(
                        name = name,
                        classType = classType,
                        ksClassType = type,
                        isDataClass = declaration.isData,
                        type = ClassModel.Type.View(
                            ModelViewQueryProperties(args.expectedArg("query"))
                        ),
                        properties = args.toViewProperties(),
                        fields = fields,
                        hasImmutableConstructor = !hasDefaultConstructor,
                        isInternal = declaration.isInternal,
                        originatingSource = originatingSource,
                        indexGroups = emptyList(),
                        uniqueGroups = emptyList(),
                        implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                        implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                        granularNotifications = false,
                    )
                )
            }
            query != null -> listOf(
                ClassModel(
                    name = name,
                    classType = classType,
                    ksClassType = type,
                    isDataClass = declaration.isData,
                    type = ClassModel.Type.Query,
                    properties = query.toAnnotationMap().toQueryProperties(),
                    fields = fields,
                    hasImmutableConstructor = !hasDefaultConstructor,
                    isInternal = declaration.isInternal,
                    originatingSource = originatingSource,
                    indexGroups = emptyList(),
                    uniqueGroups = emptyList(),
                    implementsLoadFromCursorListener = implementsLoadFromCursorListener,
                    implementsDatabaseStatementListener = implementsSQLiteStatementListener,
                    granularNotifications = false,
                )
            )
            else -> emptyList()
        }
    }

    private fun manyToManyModel(
        input: IrClass,
        classType: ClassName,
        name: NameModel,
        annotation: org.jetbrains.kotlin.ir.expressions.IrConstructorCall,
        originatingSource: IrOriginatingSource,
    ): ManyToManyModel {
        val table = input.annotation(IrTypeFqNames.Table)
            ?: throw Validation.MissingTable("ManyToMany", classType).exception
        return ManyToManyModel(
            name = name,
            properties = annotation.toAnnotationMap().toManyToManyProperties(),
            classType = classType,
            databaseTypeName = table.toAnnotationMap().toTableProperties().database,
            ksType = IrClassType(input.defaultType),
            originatingSource = originatingSource,
        )
    }

    private fun findDefaultConstructor(input: IrClass): IrConstructor? {
        val primary = input.primaryConstructor?.takeIf { ctor ->
            ctor.regularParameters.all { it.hasDefaultValue() }
        }
        if (primary != null) return primary
        return input.constructors.firstOrNull { ctor ->
            ctor.regularParameters.isEmpty() ||
                ctor.regularParameters.all { it.hasDefaultValue() }
        }
    }

    private fun validateMatchingConstructor(
        input: IrClass,
        classType: ClassName,
        fields: List<com.dbflow5.codegen.shared.FieldModel>,
    ) {
        input.constructors.firstOrNull { ctor ->
            ctor.regularParameters.size == fields.size &&
                fields.all { field ->
                    ctor.regularParameters.any { parameter ->
                        parameter.name.asString() == field.name.shortName &&
                            parameter.type.toPoetTypeName() == field.classType
                    }
                }
        } ?: throw Validation.InvalidConstructor(
            classType,
            fieldsCount = fields.size,
        ).exception
    }
}

internal val IrFunction.regularParameters
    get() = parameters.filter { it.kind == IrParameterKind.Regular }
