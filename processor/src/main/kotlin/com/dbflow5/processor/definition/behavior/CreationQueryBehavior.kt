package com.dbflow5.processor.definition.behavior

import com.dbflow5.processor.definition.TypeAdder
import com.dbflow5.processor.utils.`override fun`
import com.grosner.kpoet.L
import com.grosner.kpoet.`return`
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

/**
 * Description:
 */
data class CreationQueryBehavior(val createWithDatabase: Boolean) : TypeAdder {

    override fun addToType(typeBuilder: TypeSpec.Builder) {
        if (!createWithDatabase) {
            `override fun`(TypeName.BOOLEAN, "createWithDatabase") {
                modifiers(public, final)
                `return`(false.L)
            }
        }
    }
}