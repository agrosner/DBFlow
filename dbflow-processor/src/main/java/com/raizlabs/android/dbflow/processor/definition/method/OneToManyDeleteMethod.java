package com.raizlabs.android.dbflow.processor.definition.method;

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

    public OneToManyDeleteMethod(TableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    @Override
    public MethodSpec getMethodSpec() {
        boolean shouldWrite = false;
        for (OneToManyDefinition oneToManyDefinition : tableDefinition.oneToManyDefinitions) {
            if (oneToManyDefinition.isDelete()) {
                shouldWrite = true;
                break;
            }
        }

        if (shouldWrite) {

            CodeBlock.Builder oneToManyDefs = CodeBlock.builder();
            for (OneToManyDefinition oneToManyDefinition : tableDefinition.oneToManyDefinitions) {
                oneToManyDefinition.writeDelete(oneToManyDefs);
            }
            oneToManyDefs.addStatement("super.delete($L)", ModelUtils.getVariable(isModelContainerAdapter));

            return MethodSpec.methodBuilder("delete")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(tableDefinition.elementClassName, ModelUtils.getVariable(isModelContainerAdapter))
                    .addCode(oneToManyDefs.build())
                    .returns(TypeName.VOID).build();
        }
        return null;
    }
}
