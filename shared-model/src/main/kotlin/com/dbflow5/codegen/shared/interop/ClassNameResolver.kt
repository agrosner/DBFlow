package com.dbflow5.codegen.shared.interop

import com.squareup.kotlinpoet.ClassName

/**
 * Description:
 */
interface ClassNameResolver {

    fun classDeclarationByClassName(className: ClassName): ClassDeclaration?
}