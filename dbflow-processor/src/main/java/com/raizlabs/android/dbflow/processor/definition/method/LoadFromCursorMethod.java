package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.OneToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class LoadFromCursorMethod implements MethodDefinition {

    public static final String PARAM_MODEL = "model";
    public static final String PARAM_CURSOR = "cursor";

    private BaseTableDefinition baseTableDefinition;
    private final boolean isModelContainerAdapter;
    private final boolean implementsLoadFromCursorListener;
    private final boolean putNullForContainerAdapter;

    public LoadFromCursorMethod(BaseTableDefinition baseTableDefinition, boolean isModelContainerAdapter,
                                boolean implementsLoadFromCursorListener, boolean putNullForContainerAdapter) {

        this.baseTableDefinition = baseTableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
        this.implementsLoadFromCursorListener = implementsLoadFromCursorListener;
        this.putNullForContainerAdapter = putNullForContainerAdapter;
    }

    @Override
    public MethodSpec getMethodSpec() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadFromCursor")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ClassNames.CURSOR, PARAM_CURSOR)
                .addParameter(baseTableDefinition.getParameterClassName(isModelContainerAdapter),
                        ModelUtils.getVariable(isModelContainerAdapter))
                .returns(TypeName.VOID);

        List<ColumnDefinition> columnDefinitionList = baseTableDefinition.getColumnDefinitions();
        for (ColumnDefinition columnDefinition : columnDefinitionList) {
            methodBuilder.addCode(columnDefinition.getLoadFromCursorMethod(isModelContainerAdapter,
                    putNullForContainerAdapter, true));
        }

        if (baseTableDefinition instanceof TableDefinition && !isModelContainerAdapter) {
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            List<OneToManyDefinition> oneToManyDefinitions = ((TableDefinition) baseTableDefinition).oneToManyDefinitions;
            for (OneToManyDefinition oneToMany : oneToManyDefinitions) {
                if (oneToMany.isLoad()) {
                    oneToMany.writeLoad(codeBuilder);
                }
            }
            methodBuilder.addCode(codeBuilder.build());
        }

        if (baseTableDefinition instanceof TableDefinition && ((TableDefinition) baseTableDefinition).implementsLoadFromCursorListener) {
            methodBuilder.addStatement("$L.onLoadFromCursor($L)", ModelUtils.getVariable(isModelContainerAdapter), LoadFromCursorMethod.PARAM_CURSOR);
        }

        return methodBuilder.build();
    }
}
