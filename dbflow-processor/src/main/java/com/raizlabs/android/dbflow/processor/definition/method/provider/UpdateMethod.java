package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class UpdateMethod implements MethodDefinition {

    private static final String PARAM_URI = "uri";
    private static final String PARAM_CONTENT_VALUES = "values";

    private final ContentProviderDefinition contentProviderDefinition;

    private final ProcessorManager manager;

    public UpdateMethod(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("update")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassNames.URI, PARAM_URI)
                .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                .addParameter(ClassName.get(String.class), "selection")
                .addParameter(ArrayTypeName.of(String.class), "selectionArgs")
                .returns(TypeName.INT);

        method.beginControlFlow("switch(MATCHER.match($L))", PARAM_URI);
        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.updateEnabled) {


                    method.beginControlFlow("case $L:", uriDefinition.name);
                    method.addStatement(
                        "$T adapter = $T.getModelAdapter($T.getTableClassForName($S, $S))",
                        ClassNames.MODEL_ADAPTER,
                        ClassNames.FLOW_MANAGER,
                        ClassNames.FLOW_MANAGER,
                        contentProviderDefinition.databaseNameString,
                        tableEndpointDefinition.tableName);
                    method.addCode(ProviderMethodUtils.getSegmentsPreparation(uriDefinition));
                    method.addCode(
                        "long count = $T.getDatabase($S).getWritableDatabase().updateWithOnConflict($S, $L, ",
                        ClassNames.FLOW_MANAGER,
                        manager.getDatabaseName(contentProviderDefinition.databaseName),
                        tableEndpointDefinition.tableName,
                        PARAM_CONTENT_VALUES);
                    method.addCode(ProviderMethodUtils.getSelectionAndSelectionArgs(uriDefinition));
                    method.addCode(
                        ", $T.getSQLiteDatabaseAlgorithmInt(adapter.getUpdateOnConflictAction()));\n",
                        ClassNames.CONFLICT_ACTION);

                    CodeBlock.Builder code = CodeBlock.builder();
                    new NotifyMethod(tableEndpointDefinition, uriDefinition,
                            Notify.Method.UPDATE).addCode(code);
                    method.addCode(code.build());

                    method.addStatement("return (int) count");
                    method.endControlFlow();
                }
            }

        }

        method.beginControlFlow("default:")
                .addStatement("throw new $T($S + $L)", ClassName.get(IllegalStateException.class), "Unknown Uri", PARAM_URI)
                .endControlFlow();

        method.endControlFlow();

        return method.build();
    }
}
