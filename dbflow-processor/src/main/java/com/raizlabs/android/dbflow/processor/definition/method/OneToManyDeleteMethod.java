package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.OneToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * Description:
 */
public class OneToManyDeleteMethod implements MethodDefinition {

    private final TableDefinition tableDefinition;

    private final boolean isModelContainerAdapter;
    private final boolean useWrapper;

    public OneToManyDeleteMethod(TableDefinition tableDefinition, boolean isModelContainerAdapter,
                                 boolean useWrapper) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
        this.useWrapper = useWrapper;
    }

    @Override
    public MethodSpec getMethodSpec() {
        boolean shouldWrite = false;
        for (OneToManyDefinition oneToManyDefinition : tableDefinition.getOneToManyDefinitions()) {
            if (oneToManyDefinition.isDelete()) {
                shouldWrite = true;
                break;
            }
        }

        if (shouldWrite || !isModelContainerAdapter && tableDefinition.getCachingEnabled()) {

            CodeBlock.Builder builder = CodeBlock.builder();
            for (OneToManyDefinition oneToManyDefinition : tableDefinition.getOneToManyDefinitions()) {
                oneToManyDefinition.writeDelete(builder, useWrapper);
            }

            if (!isModelContainerAdapter && tableDefinition.getCachingEnabled()) {
                builder.addStatement("getModelCache().removeModel(getCachingId($L))", ModelUtils.getVariable());
            }

            builder.addStatement("super.delete($L$L)", ModelUtils.getVariable(),
                    useWrapper ? (", " + ModelUtils.getWrapper()) : "");

            MethodSpec.Builder delete = MethodSpec.methodBuilder("delete")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(tableDefinition.elementClassName, ModelUtils.getVariable())
                    .addCode(builder.build())
                    .returns(TypeName.VOID);
            if (useWrapper) {
                delete.addParameter(ClassNames.DATABASE_WRAPPER, ModelUtils.getWrapper());
            }
            return delete.build();
        }
        return null;
    }
}
