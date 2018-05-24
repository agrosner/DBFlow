package com.dbflow5.processor.definition

import com.squareup.javapoet.TypeSpec

/**
 * Description: Simple interface for returning a [TypeSpec].
 */
interface TypeDefinition {

    /**
     * @return The [TypeSpec] used to write this class' type file.
     */
    val typeSpec: TypeSpec
}

