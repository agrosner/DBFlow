package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.typeNameOf
import org.jetbrains.annotations.NotNull

/**
 * Description:
 */
fun TypeName.shouldBeNonNull(): Boolean {
    this == ClassNames.AndroidNonNull
        || this == ClassNames.AndroidXNonNull
        || this == typeNameOf<NotNull>()
}