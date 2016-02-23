package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class QueryMethod implements MethodDefinition {

    private final ContentProviderDefinition contentProviderDefinition;

    private final ProcessorManager manager;

    public QueryMethod(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("query")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.URI, "uri")
                .addParameter(ArrayTypeName.of(String.class), "projection")
                .addParameter(ClassName.get(String.class), "selection")
                .addParameter(ArrayTypeName.of(String.class), "selectionArgs")
                .addParameter(ClassName.get(String.class), "sortOrder")
                .returns(ClassNames.CURSOR);

        method.addStatement("$L cursor = null", ClassNames.CURSOR);
        method.beginControlFlow("switch($L.match(uri))", ContentProviderDefinition.URI_MATCHER);
        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            TableDefinition tableDefinition = manager.getTableDefinition(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.queryEnabled) {
                    method.beginControlFlow("case $L:", uriDefinition.name);
                    CodeBlock.Builder codeBuilder = CodeBlock.builder()
                            .add("cursor = $T.select(toProperties($L, projection))\n.from", ClassNames.SQLITE,
                                    CodeBlock.builder()
                                            .add("$L.PROPERTY_CONVERTER", tableDefinition.getPropertyClassName())
                                            .build());
                    ProviderMethodUtils.appendTableName(codeBuilder,
                            manager.getDatabaseName(contentProviderDefinition.databaseName), tableEndpointDefinition.tableName);
                    if (contentProviderDefinition.useSafeQueryChecking) {
                        codeBuilder.add(".where(toConditions(selection, selectionArgs))");
                    } else {
                        codeBuilder.add(".where(new $T(selection, selectionArgs))", ClassNames.UNSAFE_STRING_CONDITION);
                    }
                    ProviderMethodUtils.appendPathSegments(codeBuilder, manager, uriDefinition.segments,
                            contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);
                    if (contentProviderDefinition.useSafeQueryChecking) {
                        codeBuilder.add(".orderByAll(toOrderBy($L, $L))", "sortOrder", CodeBlock.builder()
                                .add("$L.PROPERTY_CONVERTER", tableDefinition.getPropertyClassName())
                                .build());
                    } else {
                        codeBuilder.add(".orderBy($T.fromString($L))", ClassNames.ORDER_BY, "sortOrder");
                    }
                    codeBuilder.add(".query();\n");
                    method.addCode(codeBuilder.build());
                    method.addStatement("break");
                    method.endControlFlow();
                }
            }
        }
        method.endControlFlow();

        method.beginControlFlow("if (cursor != null)");
        method.addStatement("cursor.setNotificationUri(getContext().getContentResolver(), uri)");
        method.endControlFlow();
        method.addStatement("return cursor");

        return method.build();
    }
}
