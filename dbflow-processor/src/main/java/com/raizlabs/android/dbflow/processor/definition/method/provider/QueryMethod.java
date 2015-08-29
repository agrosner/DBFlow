package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.SqlQueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
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
                .addParameter(ArrayTypeName.get(String.class), "projection")
                .addParameter(ClassName.get(String.class), "selection")
                .addParameter(ArrayTypeName.get(String.class), "selectionArgs")
                .addParameter(ClassName.get(String.class), "sortOrder");

        method.addStatement("$L cursor = null", ClassNames.CURSOR);
        method.beginControlFlow("switch($L.match(uri))", ContentProviderDefinition.URI_MATCHER);
        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.queryEnabled) {
                    method.beginControlFlow("case $L:", uriDefinition.name);

                    SqlQueryBuilder select = new SqlQueryBuilder("cursor = ")
                            .appendSelect()
                            .appendFromTable(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName)
                            .appendWhere().appendPathSegments(manager, contentProviderDefinition.databaseName,
                                    tableEndpointDefinition.tableName, uriDefinition.segments)
                            .appendQuery();
                    method.addStatement(select.getQuery());
                    method.addStatement("break");
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
