package com.dbflow5.processor.definition.behavior

import com.dbflow5.annotation.ModelCacheField
import com.dbflow5.annotation.MultiCacheField
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.utils.`override fun`
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.ensureVisibleStatic
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.quote
import com.grosner.kpoet.S
import com.grosner.kpoet.`return`
import com.grosner.kpoet.final
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.public
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

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
        val allFields: Boolean) {

    fun writeTableName(typeSpec: TypeSpec.Builder) {
        typeSpec.apply {
            `override fun`(String::class, "getTableName") {
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
        val assignDefaultValuesFromCursor: Boolean = true)

/**
 * Describes caching behavior of a [TableDefinition].
 */
data class CachingBehavior(
        val cachingEnabled: Boolean,
        val customCacheSize: Int,
        var customCacheFieldName: String?,
        var customMultiCacheFieldName: String?) {

    fun clear() {
        customCacheFieldName = null
        customMultiCacheFieldName = null
    }

    /**
     * If applicable, we store the [customCacheFieldName] or [customMultiCacheFieldName] for reference.
     */
    fun evaluateElement(element: Element, typeElement: TypeElement, manager: ProcessorManager) {
        if (element.annotation<ModelCacheField>() != null) {
            ensureVisibleStatic(element, typeElement, "ModelCacheField")
            if (!customCacheFieldName.isNullOrEmpty()) {
                manager.logError("ModelCacheField can only be declared once from: $typeElement")
            } else {
                customCacheFieldName = element.simpleName.toString()
            }
        } else if (element.annotation<MultiCacheField>() != null) {
            ensureVisibleStatic(element, typeElement, "MultiCacheField")
            if (!customMultiCacheFieldName.isNullOrEmpty()) {
                manager.logError("MultiCacheField can only be declared once from: $typeElement")
            } else {
                customMultiCacheFieldName = element.simpleName.toString()
            }
        }
    }
}

