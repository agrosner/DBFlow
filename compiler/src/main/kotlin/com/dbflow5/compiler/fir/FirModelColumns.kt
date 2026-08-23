package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.hasAnnotationSafe
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Source compilations are indexed by [predicateBasedProvider]. Dependent
 * compilations load `@Table` types from metadata, where that index is empty;
 * fall back to already-resolved annotation class ids only (unresolved
 * [org.jetbrains.kotlin.fir.types.FirUserTypeRef] annotations are skipped so
 * companion generation does not crash mid-resolution).
 */
@OptIn(SymbolInternals::class)
internal fun FirClassSymbol<*>.isDbFlowModel(session: FirSession): Boolean =
    session.predicateBasedProvider.matches(DBFLOW_MODEL_PREDICATE, this) ||
        DBFLOW_MODEL_CLASS_IDS.any { classId ->
            annotations.hasAnnotationSafe(classId, session)
        } ||
        annotations.any { it.resolvedDbFlowModelClassId() != null }

internal fun FirClassSymbol<*>.isSourceDeclaration(): Boolean =
    origin == FirDeclarationOrigin.Source

internal fun FirClassSymbol<*>.isPluginGeneratedCompanion(): Boolean {
    val origin = origin
    return origin is FirDeclarationOrigin.Plugin && origin.key == DbFlowCompanionKey
}

/**
 * Metadata serialization re-queries this extension from a different fragment
 * session than the one that generated the companion (MPP platform compiles
 * serialize commonMain classes with the leaf session). By then the companion
 * is already attached, so the answer must stay stable: keep reporting the
 * plugin companion, otherwise JVM metadata omits it from `nestedClassName`
 * and dependent compilations cannot resolve `Model.Companion` at all.
 */
internal fun FirClassSymbol<*>.generatedCompanionDecision(session: FirSession): GeneratedCompanionDecision {
    if (origin is FirDeclarationOrigin.Plugin) return GeneratedCompanionDecision.None
    if (!isDbFlowModel(session)) return GeneratedCompanionDecision.None
    val existing = existingCompanionSymbol ?: return GeneratedCompanionDecision.Create
    return if (existing.isPluginGeneratedCompanion()) {
        GeneratedCompanionDecision.ReturnExisting(existing)
    } else {
        GeneratedCompanionDecision.None
    }
}

internal sealed interface GeneratedCompanionDecision {
    data object None : GeneratedCompanionDecision
    data object Create : GeneratedCompanionDecision
    data class ReturnExisting(
        val companion: FirRegularClassSymbol,
    ) : GeneratedCompanionDecision
}

private fun FirAnnotation.resolvedDbFlowModelClassId() =
    ((annotationTypeRef as? FirResolvedTypeRef)?.coneType as? ConeClassLikeType)
        ?.lookupTag
        ?.classId
        ?.takeIf { it in DBFLOW_MODEL_CLASS_IDS }

internal fun FirClassSymbol<*>.isCompanionObject(): Boolean =
    classKind == ClassKind.OBJECT && name == SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT

internal fun FirClassSymbol<*>.ownerDbFlowModel(session: FirSession): FirClassSymbol<*>? {
    if (!isCompanionObject()) return null
    val ownerId = classId.outerClassId ?: return null
    val owner = session.symbolProvider.getClassLikeSymbolByClassId(ownerId) as? FirClassSymbol<*>
        ?: return null
    return owner.takeIf { it.isDbFlowModel(session) }
}

@OptIn(SymbolInternals::class)
internal val FirClassSymbol<*>.existingCompanionSymbol: FirRegularClassSymbol?
    get() = (this as? FirRegularClassSymbol)?.companionObjectSymbol

internal data class FirModelColumn(
    val propertyName: Name,
    val dbName: String,
    val valueType: ConeKotlinType,
)

@OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
internal fun FirClassSymbol<*>.columnSourceProperties(): List<FirProperty> {
    val properties = LinkedHashMap<Name, FirProperty>()
    fun add(property: FirProperty) {
        if (property.name.isSpecial) return
        if (property.skipAsCompanionColumn()) return
        if (property.columnAnnotation().hasCustomTypeConverter()) return
        if (!property.hasNativeColumnType()) return
        properties.putIfAbsent(property.companionColumnName(), property)
    }
    runCatching {
        (this as? FirRegularClassSymbol)?.fir?.declarations
            ?.filterIsInstance<FirProperty>()
            ?.forEach(::add)
    }
    if (properties.isEmpty()) {
        runCatching {
            declarationSymbols.forEach { symbol ->
                (symbol as? FirPropertySymbol)?.fir?.let(::add)
            }
        }
    }
    return properties.values.toList()
}

internal fun FirClassSymbol<*>.columnPropertyNames(): Set<Name> =
    columnSourceProperties().mapTo(mutableSetOf()) { property ->
        property.companionColumnName()
    }

internal fun FirClassSymbol<*>.modelColumn(
    name: Name,
    fallbackType: ConeKotlinType,
): FirModelColumn? {
    val property = columnSourceProperties().firstOrNull { it.companionColumnName() == name }
        ?: return null
    val dbName = property.companionColumnDbName()
    val valueType = (property.returnTypeRef as? FirResolvedTypeRef)?.coneType
        ?: runCatching { property.symbol.resolvedReturnType }.getOrNull()
        ?: fallbackType
    return FirModelColumn(
        propertyName = name,
        dbName = dbName,
        valueType = valueType,
    )
}

/**
 * Names that already resolve without this plugin. Klib metadata can list
 * plugin-generated `name`/`table` properties that consumers do not put in
 * the callable scope; those must be regenerated, so they are ignored here.
 */
@OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
internal fun FirClassSymbol<*>.existingMemberNames(): Set<Name> {
    val names = mutableSetOf<Name>()
    fun addIfResolvable(property: FirProperty) {
        if (property.origin.isDbFlowGenerated()) return
        names.add(property.name)
    }
    runCatching {
        (this as? FirRegularClassSymbol)?.fir?.declarations?.forEach { declaration ->
            (declaration as? FirProperty)?.let(::addIfResolvable)
        }
    }
    runCatching {
        declarationSymbols.forEach { symbol ->
            (symbol as? FirPropertySymbol)?.fir?.let(::addIfResolvable)
        }
    }
    return names
}

private fun FirDeclarationOrigin.isDbFlowGenerated(): Boolean =
    this is FirDeclarationOrigin.Plugin &&
        (key == DbFlowColumnPropertyKey || key == DbFlowCompanionPropertyKey)

private fun FirProperty.columnAnnotation(): FirAnnotation? =
    annotations.firstOrNull { it.resolvedAnnotationClassId() == DbFlowClassIds.Column }

private fun FirProperty.companionColumnName(): Name {
    val dbName = columnAnnotation()?.constString("name")?.takeIf { it.isNotBlank() }
    return if (dbName != null) Name.identifier(dbName) else name
}

private fun FirProperty.companionColumnDbName(): String =
    columnAnnotation()?.constString("name")?.takeIf { it.isNotBlank() } ?: name.asString()

private fun FirProperty.skipAsCompanionColumn(): Boolean =
    annotations.any { annotation ->
        annotation.resolvedAnnotationClassId() in SKIPPED_COLUMN_ANNOTATIONS
    }

private val SKIPPED_COLUMN_ANNOTATIONS = setOf(
    DbFlowClassIds.ColumnIgnore,
    DbFlowClassIds.ForeignKey,
    DbFlowClassIds.ColumnMap,
)

/**
 * Only columns stored natively by codegen get plain [com.dbflow5.query.operations.PropertyStart]
 * companion members. Everything else (Boolean, Char, Blob, enums, custom or
 * globally converted types) is written by KotlinPoet as a
 * `TypeConvertedProperty` extension; a plain member here would shadow that
 * extension with the wrong type. Unresolved types are skipped for the same
 * reason.
 */
private fun FirProperty.hasNativeColumnType(): Boolean {
    val coneType = (returnTypeRef as? FirResolvedTypeRef)?.coneType ?: return false
    val classId = (coneType as? ConeClassLikeType)?.lookupTag?.classId ?: return false
    return classId in NATIVE_COLUMN_CLASS_IDS
}

private val NATIVE_COLUMN_CLASS_IDS = setOf(
    "String",
    "Int",
    "Long",
    "Double",
    "Float",
    "ByteArray",
).mapTo(mutableSetOf()) { name ->
    ClassId.topLevel(FqName("kotlin.$name"))
}

private fun FirAnnotation.resolvedAnnotationClassId() =
    ((annotationTypeRef as? FirResolvedTypeRef)?.coneType as? ConeClassLikeType)
        ?.lookupTag
        ?.classId

private fun FirAnnotation.constString(name: String): String? = runCatching {
    val expression = argumentMapping.mapping[Name.identifier(name)] ?: return null
    (expression as? FirLiteralExpression)?.value as? String
}.getOrNull()

private fun FirAnnotation?.hasCustomTypeConverter(): Boolean {
    if (this == null) return false
    val expression = runCatching {
        argumentMapping.mapping[Name.identifier("typeConverter")]
    }.getOrNull() ?: return false
    val classId = expression.typeConverterClassId() ?: return true
    return classId != DbFlowClassIds.TypeConverter
}

private fun org.jetbrains.kotlin.fir.expressions.FirExpression.typeConverterClassId(): ClassId? =
    when (this) {
        is FirGetClassCall -> (argument as? FirResolvedQualifier)?.classId
        is FirResolvedQualifier -> classId
        else -> null
    }
