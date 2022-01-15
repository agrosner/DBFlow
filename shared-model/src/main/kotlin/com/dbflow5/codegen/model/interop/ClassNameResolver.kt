package com.dbflow5.codegen.model.interop

import com.squareup.kotlinpoet.ClassName

/**
 * Description:
 */
interface ClassNameResolver {

    fun classDeclarationByClassName(className: ClassName): ClassDeclaration?
}