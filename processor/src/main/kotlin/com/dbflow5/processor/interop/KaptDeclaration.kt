package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.interop.ClassDeclaration
import com.dbflow5.codegen.shared.interop.Declaration
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.toTypeElement
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

/**
 * Description:
 */
class KaptDeclaration(
    private val typeMirror: TypeMirror,
    element: Element,
) : Declaration {
    override val simpleName: NameModel = NameModel(
        element.simpleName,
        element.getPackage(),
    )

    /**
     * way kapt works, this is the same.
     */
    override val closestClassDeclaration: ClassDeclaration? by lazy {
        KaptClassDeclaration(typeMirror.toTypeElement())
    }

    override fun hasValueModifier(): Boolean {
        return false // kapt has no clue
    }
}