package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Description:
 */
public class ProviderMethodUtils {


    static void appendTableName(CodeBlock.Builder codeBuilder, String databaseName, String tableName) {
        codeBuilder.add("(FlowManager.getTableClassForName($S, $S))", databaseName, tableName);
    }

    static void appendPathSegments(CodeBlock.Builder codeBuilder, ProcessorManager processorManager,
                                   ContentUri.PathSegment[] pathSegments, TypeName databaseName, String tableName) {
        TableDefinition tableDefinition = processorManager.getTableDefinition(databaseName, tableName);
        if (tableDefinition == null) {
            processorManager.logError("Could not find table definition for %1s from %1s", tableName, databaseName);
        } else {
            for (ContentUri.PathSegment pathSegment : pathSegments) {
                ColumnDefinition columnDefinition = tableDefinition.mColumnMap.get(pathSegment.column());
                if (columnDefinition == null) {
                    processorManager.logError("Column %1s not found for table %1s", pathSegment.column(), tableDefinition.tableName);
                } else {
                    codeBuilder.add("\n.and($T.$L.is(", tableDefinition.getPropertyClassName(),
                            pathSegment.column());

                    // primitive use value of
                    if (columnDefinition.element.asType().getKind().isPrimitive()) {
                        String name = columnDefinition.element.asType().toString();

                        // handle char
                        if ("char".equals(name)) {
                            name = "character";
                        }

                        // handle integer
                        if ("int".equals(name)) {
                            name = "integer";
                        }

                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        codeBuilder.add("$L.valueOf(uri.getPathSegments().get($L))", name, pathSegment.segment());
                    } else {
                        codeBuilder.add("uri.getPathSegments().get($L)", pathSegment.segment());
                    }
                    codeBuilder.add("))");
                }
            }
        }
    }
}
