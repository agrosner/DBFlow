package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.ClassNameResolver
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName

/**
 * Description: KSP implementation of [ClassNameResolver]
 */
class KSPResolver(
    private val resolver: Resolver
) : ClassNameResolver {

    override fun classDeclarationByClassName(className: ClassName): ClassDeclaration? {
        return KSPClassDeclaration(
            resolver.getClassDeclarationByName(
                resolver.getKSNameFromString(
                    className.toString()
                )
            )
        )
    }
}