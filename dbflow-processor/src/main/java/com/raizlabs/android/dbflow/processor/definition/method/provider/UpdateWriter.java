package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.SqlQueryBuilder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class UpdateWriter implements MethodDefinition {

    private static final String PARAM_URI = "uri";
    private static final String PARAM_CONTENT_VALUES = "values";

    private final ContentProviderDefinition contentProviderDefinition;

    private final ProcessorManager manager;

    public UpdateWriter(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public MethodSpec getMethodSpec() {
        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("switch(MATCHER.match($L))", PARAM_URI);
        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.updateEnabled) {
                    code.beginControlFlow("case %1s:", uriDefinition.name);
                    code.addStatement("$T adapter = $T.getModelAdapter($T.getTableClassForName($S, $S))",
                            ClassNames.MODEL_ADAPTER, ClassNames.FLOW_MANAGER, ClassNames.FLOW_MANAGER,
                            contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);

                    SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder("final int count = (int) ")
                            .appendUpdate(contentProviderDefinition.databaseName, tableEndpointDefinition.tableName).appendUpdateConflictAction()
                            .appendSet().appendWhere().appendPathSegments(manager, contentProviderDefinition.databaseName,
                                    tableEndpointDefinition.tableName, uriDefinition.segments)
                            .appendCount();
                    code.addStatement(sqlQueryBuilder.getQuery());

                    new NotifyMethod(tableEndpointDefinition, uriDefinition,
                            Notify.Method.UPDATE).addCode(code);

                    code.addStatement("return count");
                    code.endControlFlow();
                }
            }

        }

        code.beginControlFlow("default:")
                .addStatement("throw new $T($S + $L)", ClassName.get(IllegalStateException.class), "Unknown Uri", PARAM_URI)
                .endControlFlow();

        code.endControlFlow();

        return MethodSpec.methodBuilder("update")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassNames.URI, PARAM_URI)
                .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                .addParameter(ClassName.get(String.class), "selection")
                .addParameter(ArrayTypeName.get(String.class), "selectionArgs")
                .returns(TypeName.INT).build();
    }
}
