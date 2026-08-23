package com.dbflow5.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotationSafe
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef

internal class FirTableCompanionSupertypeExtension(
    session: FirSession,
) : FirSupertypeGenerationExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(DBFLOW_MODEL_PREDICATE)
    }

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean {
        val firClass = declaration as? FirClass ?: return false
        if (!firClass.symbol.isPluginGeneratedCompanion()) return false
        val ownerId = firClass.symbol.classId.outerClassId ?: return false
        val owner = session.symbolProvider.getClassLikeSymbolByClassId(ownerId) as? FirClassSymbol<*>
            ?: return false
        return owner.isDbFlowModel(session)
    }

    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
        typeResolver: TypeResolveService,
    ): List<ConeKotlinType> {
        val ownerId = classLikeDeclaration.symbol.classId.outerClassId ?: return emptyList()
        val owner = session.symbolProvider.getClassLikeSymbolByClassId(ownerId) as? FirClassSymbol<*>
            ?: return emptyList()
        val additional = mutableListOf<ConeKotlinType>()
        val alreadyHasAdapterCompanion = resolvedSupertypes.any { typeRef ->
            typeRef.coneType.classId == DbFlowClassIds.AdapterCompanion
        }
        if (!alreadyHasAdapterCompanion) {
            additional += DbFlowClassIds.AdapterCompanion.createConeType(
                session,
                arrayOf(owner.defaultType()),
            )
        }
        return additional
    }
}

private val ConeKotlinType.classId
    get() = (this as? ConeClassLikeType)?.lookupTag?.classId
