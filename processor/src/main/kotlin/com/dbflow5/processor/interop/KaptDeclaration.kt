package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.interop.ClassDeclaration
import com.dbflow5.codegen.model.interop.Declaration
import com.dbflow5.processor.utils.getPackage
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class KaptDeclaration(
    private val typeElement: TypeElement,
) : Declaration {
    override val simpleName: NameModel = NameModel(
        typeElement.simpleName,
        typeElement.getPackage(),
    )

    /**
     * way kapt works, this is the same.
     */
    override val closestClassDeclaration: ClassDeclaration? by lazy {
        KaptClassDeclaration(typeElement)
    }

    override fun hasValueModifier(): Boolean {
        return false // kapt has no clue
    }
}