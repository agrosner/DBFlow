package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.squareup.kotlinpoet.ClassName
import javax.lang.model.util.Elements

/**
 * Description: Resolves elements to class names.
 */
class KaptResolver(
    private val elements: Elements,
) : ClassNameResolver {
    override fun classDeclarationByClassName(className: ClassName): ClassDeclaration {
        return KaptClassDeclaration(elements.getTypeElement(className.canonicalName))
    }
}
