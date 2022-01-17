package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.getPackage
import com.grosner.kpoet.typeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class KaptPropertyDeclaration(
    element: VariableElement,
) : PropertyDeclaration {

    override val typeName: TypeName = element.asType().typeName.toKTypeName()
    override val simpleName: NameModel =
        NameModel(
            packageName = element.getPackage(ProcessorManager.manager).simpleName.toString(),
            shortName = element.simpleName.toString(),
        )
}