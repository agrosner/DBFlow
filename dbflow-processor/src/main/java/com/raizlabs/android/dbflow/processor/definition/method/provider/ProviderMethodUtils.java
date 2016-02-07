package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.List;

/**
 * Description:
 */
public class ProviderMethodUtils {

    /**
     * Get any code needed to use path segments. This should be called before creating the statement that uses
     * {@link #getSelectionAndSelectionArgs(ContentUriDefinition)}.
     */
    static CodeBlock getSegmentsPreparation(ContentUriDefinition uriDefinition) {
        if (uriDefinition.segments.length == 0) {
            return CodeBlock.builder().build();
        } else {
            return CodeBlock.builder()
                .addStatement("$T<$T> segments = uri.getPathSegments()", List.class, String.class)
                .build();
        }
    }

    /**
     * Get code which creates the {@code selection} and {@code selectionArgs} parameters separated by a comma.
     */
    static CodeBlock getSelectionAndSelectionArgs(ContentUriDefinition uriDefinition) {
        ContentUri.PathSegment[] segments = uriDefinition.segments;
        if (segments.length == 0) {
            return CodeBlock.builder().add("selection, selectionArgs").build();
        } else {
            CodeBlock.Builder selectionBuilder =
                CodeBlock.builder().add("$T.concatenateWhere(selection, \"", ClassNames.DATABASE_UTILS);
            CodeBlock.Builder selectionArgsBuilder =
                CodeBlock.builder()
                    .add("$T.appendSelectionArgs(selectionArgs, new $T[] {", ClassNames.DATABASE_UTILS, String.class);
            boolean isFirst = true;
            for (ContentUri.PathSegment segment : segments) {
                if (!isFirst) {
                    selectionBuilder.add(" AND ");
                    selectionArgsBuilder.add(", ");
                }
                selectionBuilder.add("$L = ?", segment.column());
                selectionArgsBuilder.add("segments.get($L)", segment.segment());
                isFirst = false;
            }
            selectionBuilder.add("\")");
            selectionArgsBuilder.add("})");
            return CodeBlock.builder()
                .add(selectionBuilder.build())
                .add(", ")
                .add(selectionArgsBuilder.build())
                .build();
        }
    }
}
