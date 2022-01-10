package com.dbflow5.codegen.model.interop

import com.dbflow5.codegen.model.NameModel

/**
 * Description:
 */
interface Declaration {

    val simpleName: NameModel

    val closestClassDeclaration: ClassDeclaration?

    fun hasValueModifier(): Boolean
}
