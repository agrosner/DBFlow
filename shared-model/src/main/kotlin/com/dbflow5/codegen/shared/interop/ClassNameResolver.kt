package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.ClassName

/**
 * This is injected during runtime and not ahead - as it wraps processor
 * specific instances.
 */
interface ClassNameResolver {

    fun classDeclarationByClassName(className: ClassName): ClassDeclaration?

    fun classTypeByClassName(className: ClassName): ClassType
}