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
public class DeleteMethod implements MethodDefinition {

    private static final String PARAM_URI = "uri";
    private static final String PARAM_SELECTION = "selection";
    private static final String PARAM_SELECTION_ARGS = "selectionArgs";


    private final ContentProviderDefinition contentProviderDefinition;

    private ProcessorManager manager;

    public DeleteMethod(ContentProviderDefinition contentProviderDefinition, ProcessorManager manager) {
        this.contentProviderDefinition = contentProviderDefinition;
        this.manager = manager;
    }

    @Override
    public MethodSpec getMethodSpec() {
        CodeBlock.Builder code = CodeBlock.builder();

        code.beginControlFlow("switch(MATCHER.match($L))", PARAM_URI);
        for (TableEndpointDefinition tableEndpointDefinition : contentProviderDefinition.endpointDefinitions) {
            for (ContentUriDefinition uriDefinition : tableEndpointDefinition.contentUriDefinitions) {
                if (uriDefinition.deleteEnabled) {

                    String databaseName = manager.getDatabaseName(contentProviderDefinition.databaseName);

                    code.beginControlFlow("case $L:", uriDefinition.name);

                    code.add("long count = $T.delete", ClassNames.SQLITE);
                    ProviderMethodUtils.appendTableName(code, databaseName, tableEndpointDefinition.tableName);
                    if (contentProviderDefinition.useSafeQueryChecking) {
                        code.add(".where(toConditions(selection, selectionArgs))");
                    } else {
                        code.add(".where(new $T(selection, selectionArgs))", ClassNames.UNSAFE_STRING_CONDITION);
                    }
                    ProviderMethodUtils.appendPathSegments(code, manager, uriDefinition.segments,
                            contentProviderDefinition.databaseName, tableEndpointDefinition.tableName);
                    code.add(".count();\n");

                    new NotifyMethod(tableEndpointDefinition, uriDefinition, Notify.Method.DELETE)
                            .addCode(code);

                    code.addStatement("return (int) count");
                    code.endControlFlow();
                }
            }
        }

        code.beginControlFlow("default:")
                .addStatement("throw new $T($S + $L)", ClassName.get(IllegalArgumentException.class), "Unknown URI", PARAM_URI)
                .endControlFlow();
        code.endControlFlow();

        return MethodSpec.methodBuilder("delete")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.URI, PARAM_URI)
                .addParameter(ClassName.get(String.class), PARAM_SELECTION)
                .addParameter(ArrayTypeName.of(String.class), PARAM_SELECTION_ARGS)
                .addCode(code.build())
                .returns(TypeName.INT).build();
    }

}
