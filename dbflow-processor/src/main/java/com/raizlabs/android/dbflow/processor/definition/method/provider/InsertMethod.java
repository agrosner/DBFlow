package com.raizlabs.android.dbflow.processor.definition.method.provider;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.ContentProviderDefinition;
import com.raizlabs.android.dbflow.processor.definition.ContentUriDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableEndpointDefinition;
import com.raizlabs.android.dbflow.processor.definition.method.MethodDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class InsertMethod implements MethodDefinition {

    private static final String PARAM_URI = "uri";
    private static final String PARAM_CONTENT_VALUES = "values";

    private final ContentProviderDefinition contentProviderDefinition;
    private final boolean isBulk;

    public InsertMethod(ContentProviderDefinition contentProviderDefinition, boolean isBulk) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.isBulk = isBulk;
    }

    @Override
    public MethodSpec getMethodSpec() {
        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("switch(MATCHER.match($L))", PARAM_URI);

        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.insertEnabled) {
                    code.beginControlFlow("case $L:", uriDefinition.name);
                    code.addStatement("$T adapter = $T.getModelAdapter($T.getTableClassForName($S, $S))",
                            ClassNames.MODEL_ADAPTER, ClassNames.FLOW_MANAGER, ClassNames.FLOW_MANAGER,
                            contentProviderDefinition.databaseNameString, tableEndpointDefinition.tableName);

                    code.add("final long id = FlowManager.getDatabase($S).getDatabase()",
                            contentProviderDefinition.databaseNameString)
                            .add(".insertWithOnConflict($S, null, values, " +
                                            "$T.getSQLiteDatabaseAlgorithmInt(adapter.getInsertOnConflictAction()));\n", tableEndpointDefinition.tableName,
                                    ClassNames.CONFLICT_ACTION);

                    if (!isBulk) {
                        new NotifyMethod(tableEndpointDefinition, uriDefinition,
                                Notify.Method.INSERT).addCode(code);

                        code.addStatement("return $T.withAppendedId($L, id)", ClassNames.CONTENT_URIS, PARAM_URI);
                    } else {
                        code.addStatement("return id > 0 ? 1 : 0");
                    }
                    code.endControlFlow();
                }
            }

        }

        code.beginControlFlow("default:")
                .addStatement("throw new $T($S + $L)", ClassName.get(IllegalStateException.class), "Unknown Uri", PARAM_URI)
                .endControlFlow();

        code.endControlFlow();
        return MethodSpec.methodBuilder(isBulk ? "bulkInsert" : "insert")
                .addAnnotation(Override.class)
                .addParameter(ClassNames.URI, PARAM_URI)
                .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                .addModifiers(isBulk ? Modifier.PROTECTED : Modifier.PUBLIC, Modifier.FINAL)
                .addCode(code.build())
                .returns(isBulk ? TypeName.INT : ClassNames.URI).build();
    }

}
