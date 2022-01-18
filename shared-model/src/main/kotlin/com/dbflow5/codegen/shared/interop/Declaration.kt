package com.dbflow5.codegen.shared.interop

import com.dbflow5.codegen.shared.NameModel

/**
 * Description:
 */
interface Declaration {

    val simpleName: NameModel

    val closestClassDeclaration: ClassDeclaration?

    fun hasValueModifier(): Boolean
}
