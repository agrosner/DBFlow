package com.dbflow5.ksp.model.interop

import com.dbflow5.ksp.model.invoke
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.interop.PropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * Description:
 */
data class KSPPropertyDeclaration(
    val property: KSPropertyDeclaration
) : PropertyDeclaration {
    override val simpleName: NameModel = NameModel(
        property.simpleName,
        property.packageName,
    )
}