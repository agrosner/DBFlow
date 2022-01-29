package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.interop.ClassType
import com.squareup.kotlinpoet.ClassName
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Description: Resolves elements to class names.
 */
class KaptResolver(
    private val elements: Elements,
    private val types: Types,
) : ClassNameResolver {
    override fun classDeclarationByClassName(className: ClassName): ClassDeclaration? {
        return elements.getTypeElement(className.canonicalName)?.let { KaptClassDeclaration(it) }
    }

    override fun classTypeByClassName(className: ClassName): ClassType {
        return elements.getTypeElement(className.canonicalName)!!.let {
            KaptTypeElementClassType(it.asType(), it)
        }
    }
}
