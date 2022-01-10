package com.dbflow5.model.interop

import com.dbflow5.model.NameModel

/**
 * Description:
 */
interface Declaration {

    val simpleName: NameModel

    val closestClassDeclaration: ClassDeclaration?

    fun hasValueModifier(): Boolean
}
