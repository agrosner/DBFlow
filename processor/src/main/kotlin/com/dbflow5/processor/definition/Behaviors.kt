package com.dbflow5.processor.definition

import com.dbflow5.annotation.PrimaryKey
import com.squareup.javapoet.TypeName

/**
 * Description: Defines how a class is named, db it belongs to, and other loading behaviors.
 */
data class AssociationalBehavior(
        /**
         * @return The name of this view. Default is the class name.
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
        val allFields: Boolean)

