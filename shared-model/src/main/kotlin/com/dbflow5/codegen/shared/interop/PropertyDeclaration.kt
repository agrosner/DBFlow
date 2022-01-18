package com.dbflow5.codegen.shared.interop

import com.dbflow5.codegen.shared.NameModel
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface PropertyDeclaration {

    val simpleName: NameModel

    val typeName: TypeName
}
