package com.dbflow5.codegen.model.interop

import com.dbflow5.codegen.model.NameModel
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface PropertyDeclaration {

    val simpleName: NameModel

    val typeName: TypeName
}
