package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class FirTableCompanionGenerationExtension(
    session: FirSession,
) : FirDeclarationGenerationExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DBFLOW_MODEL_PREDICATE)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        return when (classSymbol.generatedCompanionDecision(session)) {
            GeneratedCompanionDecision.None -> emptySet()
            else -> setOf(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        if (name != SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT) return null
        return when (val decision = owner.generatedCompanionDecision(session)) {
            GeneratedCompanionDecision.None -> null
            is GeneratedCompanionDecision.ReturnExisting -> decision.companion
            GeneratedCompanionDecision.Create -> createCompanionObject(owner, DbFlowCompanionKey) {
                superType(
                    DbFlowClassIds.AdapterCompanion.createConeType(
                        session,
                        arrayOf(owner.defaultType()),
                    )
                )
            }.symbol
        }
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        if (classSymbol.isPluginGeneratedCompanion()) {
            val model = classSymbol.ownerDbFlowModel(session) ?: return setOf(
                TABLE_PROPERTY,
                SpecialNames.INIT,
            )
            return setOf(TABLE_PROPERTY, SpecialNames.INIT) + model.columnPropertyNames()
        }
        val model = classSymbol.ownerDbFlowModel(session)
            ?: classSymbol.takeIf { it.isDbFlowModel(session) && it.classKind == ClassKind.OBJECT }
            ?: return emptySet()
        val existing = classSymbol.existingMemberNames()
        return (setOf(TABLE_PROPERTY) + model.columnPropertyNames()).filterNot { it in existing }.toSet()
    }

    override fun generateConstructors(
        context: MemberGenerationContext,
    ): List<FirConstructorSymbol> {
        val owner = context.owner
        if (!owner.isPluginGeneratedCompanion()) return emptyList()
        return listOf(createDefaultPrivateConstructor(owner, DbFlowCompanionKey).symbol)
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        val model = owner.ownerDbFlowModel(session)
            ?: owner.takeIf { it.isDbFlowModel(session) && it.classKind == ClassKind.OBJECT }
            ?: return emptyList()
        if (callableId.callableName == TABLE_PROPERTY) {
            return listOf(tableProperty(owner, model))
        }
        val column = model.modelColumn(
            name = callableId.callableName,
            fallbackType = session.builtinTypes.nullableAnyType.coneType,
        ) ?: return emptyList()
        return listOf(columnProperty(owner, model, column))
    }

    private fun tableProperty(
        owner: FirClassSymbol<*>,
        model: FirClassSymbol<*>,
    ): FirPropertySymbol = createMemberProperty(
        owner = owner,
        key = DbFlowCompanionPropertyKey,
        name = TABLE_PROPERTY,
        returnType = DbFlowClassIds.KClass.createConeType(
            session,
            arrayOf(model.defaultType()),
        ),
        isVal = true,
        hasBackingField = false,
    ) {
        status { isOverride = true }
    }.symbol

    private fun columnProperty(
        owner: FirClassSymbol<*>,
        model: FirClassSymbol<*>,
        column: FirModelColumn,
    ): FirPropertySymbol = createMemberProperty(
        owner = owner,
        key = DbFlowColumnPropertyKey,
        name = column.propertyName,
        returnType = DbFlowClassIds.PropertyStart.createConeType(
            session,
            arrayOf(column.valueType, model.defaultType()),
        ),
        isVal = true,
        hasBackingField = false,
    ).symbol
}
