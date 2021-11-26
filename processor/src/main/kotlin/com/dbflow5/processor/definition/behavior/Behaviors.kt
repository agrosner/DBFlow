package com.dbflow5.processor.definition.behavior

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.quote
import com.grosner.kpoet.S
import com.grosner.kpoet.`return`
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

/**
 * Defines how a class is named, db it belongs to, and other loading behaviors.
 */
data class AssociationalBehavior(
    /**
     * @return The name of this object in the database. Default is the class name.
     */
    val name: String,
    /**
     * @return The class of the database this corresponds to.
     */
    val databaseTypeName: TypeName,
    /**
     * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as [com.dbflow5.annotation.Column] .
     * The only required annotated field becomes The [PrimaryKey]
     * or [PrimaryKey.autoincrement].
     */
    val allFields: Boolean
) {

    fun writeName(typeSpec: TypeSpec.Builder) {
        typeSpec.apply {
            `override fun`(String::class, "getName") {
                modifiers(public, final)
                `return`(name.quote().S)
            }
        }
    }
}

/**
 * Defines how a Cursor gets loaded from the DB.
 */
data class CursorHandlingBehavior(
    val orderedCursorLookup: Boolean = false,
    val assignDefaultValuesFromCursor: Boolean = true
)
