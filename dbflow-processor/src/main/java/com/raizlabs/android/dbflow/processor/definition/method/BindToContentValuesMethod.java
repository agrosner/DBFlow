package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes the bind to content values method in the ModelDAO.
 */
public class BindToContentValuesMethod implements MethodDefinition {

    public static final String PARAM_CONTENT_VALUES = "values";
    public static final String PARAM_MODEL = "model";

    private BaseTableDefinition baseTableDefinition;
    private final boolean isModelContainerAdapter;
    private boolean implementsContentValuesListener;
    private boolean isInsert;

    public BindToContentValuesMethod(BaseTableDefinition baseTableDefinition, boolean isInsert, boolean isModelContainerAdapter, boolean implementsContentValuesListener) {
        this.baseTableDefinition = baseTableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
        this.implementsContentValuesListener = implementsContentValuesListener;
        this.isInsert = isInsert;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(isInsert ? "bindToInsertValues" : "bindToContentValues")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.CONTENT_VALUES, PARAM_CONTENT_VALUES)
                .addParameter(baseTableDefinition.getParameterClassName(isModelContainerAdapter),
                        ModelUtils.getVariable(isModelContainerAdapter))
                .returns(TypeName.VOID);

        List<ColumnDefinition> columnDefinitionList = baseTableDefinition.getColumnDefinitions();
        for (ColumnDefinition columnDefinition : columnDefinitionList) {
            methodBuilder.addCode(columnDefinition.getContentValuesStatement(isModelContainerAdapter));
        }

        if (implementsContentValuesListener) {
            methodBuilder.addStatement("$L.onBindTo$LValues($L)",
                    ModelUtils.getVariable(isModelContainerAdapter), isInsert ? "Insert" : "Content", PARAM_CONTENT_VALUES);
        }

        return methodBuilder.build();
    }
}
