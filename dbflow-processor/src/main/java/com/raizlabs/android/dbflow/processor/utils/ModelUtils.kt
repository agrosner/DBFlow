package com.raizlabs.android.dbflow.processor.utils

import com.raizlabs.android.dbflow.annotation.ManyToMany
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * Author: andrewgrosner
 * Description:
 */
object ModelUtils {

    val variable: String
        get() = "model"

    val wrapper: String
        get() = "wrapper"

    fun getReferencedClassFromAnnotation(annotation: ManyToMany?): TypeMirror? {
        var clazz: TypeMirror? = null
        if (annotation != null) {
            try {
                annotation.referencedTable
            } catch (mte: MirroredTypeException) {
                clazz = mte.typeMirror
            }

        }
        return clazz
    }


}
