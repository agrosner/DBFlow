package com.raizlabs.android.dbflow.processor.definition.method.provider

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock

/**
 * Get any code needed to use path segments. This should be called before creating the statement that uses
 * [.getSelectionAndSelectionArgs].
 */
internal fun ContentUriDefinition.getSegmentsPreparation(): CodeBlock {
    if (segments.size == 0) {
        return CodeBlock.builder().build()
    } else {
        return CodeBlock.builder().addStatement("\$T<\$T> segments = uri.getPathSegments()",
                List::class.java, String::class.java).build()
    }
}

/**
 * Get code which creates the `selection` and `selectionArgs` parameters separated by a comma.
 */
internal fun ContentUriDefinition.getSelectionAndSelectionArgs(): CodeBlock {
    if (segments.size == 0) {
        return CodeBlock.builder().add("selection, selectionArgs").build()
    } else {
        val selectionBuilder = CodeBlock.builder().add("\$T.concatenateWhere(selection, \"", ClassNames.DATABASE_UTILS)
        val selectionArgsBuilder = CodeBlock.builder().add("\$T.appendSelectionArgs(selectionArgs, new \$T[] {",
                ClassNames.DATABASE_UTILS, String::class.java)
        var isFirst = true
        for (segment in segments) {
            if (!isFirst) {
                selectionBuilder.add(" AND ")
                selectionArgsBuilder.add(", ")
            }
            selectionBuilder.add("\$L = ?", segment.column)
            selectionArgsBuilder.add("segments.get(\$L)", segment.segment)
            isFirst = false
        }
        selectionBuilder.add("\")")
        selectionArgsBuilder.add("})")
        return CodeBlock.builder().add(selectionBuilder.build()).add(", ").add(selectionArgsBuilder.build()).build()
    }
}