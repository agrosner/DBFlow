package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.ProcessorUtils;
import com.raizlabs.android.dbflow.processor.definition.column.BaseColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.PrivateColumnAccess;
import com.raizlabs.android.dbflow.processor.definition.column.SimpleColumnAccess;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description: Represents the {@link OneToMany} annotation.
 */
public class OneToManyDefinition extends BaseDefinition {

    public String methodName;

    public String variableName;

    public List<OneToMany.Method> methods = Lists.newArrayList();

    private BaseColumnAccess columnAccess;
    private boolean extendsBaseModel;
    private boolean extendsModel;
    private TypeName referencedTableType;
    private TypeElement referencedType;

    public OneToManyDefinition(ExecutableElement typeElement,
                               ProcessorManager processorManager) {
        super(typeElement, processorManager);

        OneToMany oneToMany = typeElement.getAnnotation(OneToMany.class);

        methodName = typeElement.getSimpleName().toString();
        variableName = oneToMany.variableName();
        if (variableName == null || variableName.isEmpty()) {
            variableName = methodName.replace("get", "");
            variableName = variableName.substring(0, 1).toLowerCase() + variableName.substring(1);
        }
        methods.addAll(Arrays.asList(oneToMany.methods()));

        if (oneToMany.isVariablePrivate()) {
            columnAccess = new PrivateColumnAccess(false);
        } else {
            columnAccess = new SimpleColumnAccess();
        }

        extendsBaseModel = false;
        TypeMirror returnType = typeElement.getReturnType();
        TypeName typeName = TypeName.get(returnType);
        if (typeName instanceof ParameterizedTypeName) {
            List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
            if (typeArguments.size() == 1) {
                referencedTableType = typeArguments.get(0);
                referencedType = getManager().getElements().getTypeElement(referencedTableType.toString());
                extendsBaseModel = ProcessorUtils.INSTANCE.isSubclass(getManager().getProcessingEnvironment(),
                        ClassNames.BASE_MODEL.toString(), referencedType);
                extendsModel = ProcessorUtils.INSTANCE.isSubclass(getManager().getProcessingEnvironment(),
                        ClassNames.MODEL.toString(), referencedType);
            }
        }
    }

    public boolean isLoad() {
        return isAll() || methods.contains(OneToMany.Method.LOAD);
    }

    public boolean isAll() {
        return methods.contains(OneToMany.Method.ALL);
    }

    public boolean isDelete() {
        return isAll() || methods.contains(OneToMany.Method.DELETE);
    }

    public boolean isSave() {
        return isAll() || methods.contains(OneToMany.Method.SAVE);
    }

    /**
     * Writes the method to the specified builder for loading from DB.
     */
    public void writeLoad(CodeBlock.Builder codeBuilder) {
        if (isLoad()) {
            codeBuilder.addStatement(getMethodName());
        }
    }

    /**
     * Writes a delete method that will delete all related objects.
     *
     * @param codeBuilder
     */
    public void writeDelete(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isDelete()) {
            writeLoopWithMethod(codeBuilder, "delete", useWrapper && extendsBaseModel);

            codeBuilder.add(columnAccess.setColumnAccessString(null, variableName, variableName,
                    ModelUtils.getVariable(), CodeBlock.builder().add("null").build())
                    .toBuilder().add(";\n").build());
        }
    }

    public void writeSave(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "save", useWrapper && extendsBaseModel);
        }
    }

    public void writeUpdate(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "update", useWrapper && extendsBaseModel);
        }
    }

    public void writeInsert(CodeBlock.Builder codeBuilder, boolean useWrapper) {
        if (isSave()) {
            writeLoopWithMethod(codeBuilder, "insert", useWrapper && extendsBaseModel
                    || useWrapper && !extendsModel);
        }
    }

    private void writeLoopWithMethod(CodeBlock.Builder codeBuilder, String methodName, boolean useWrapper) {
        codeBuilder
                .beginControlFlow("if ($L != null) ", getMethodName());
        ClassName loopClass = null;
        if (extendsBaseModel) {
            loopClass = ClassNames.BASE_MODEL;
        } else {
            loopClass = ClassName.get(referencedType);
        }

        // need to load adapter for non-model classes
        if (!extendsModel) {
            codeBuilder.addStatement("$T adapter = $T.getModelAdapter($T.class)",
                    ParameterizedTypeName.get(ClassNames.MODEL_ADAPTER, referencedTableType),
                    ClassNames.FLOW_MANAGER, referencedTableType);

            codeBuilder.addStatement("adapter.$LAll($L$L)", methodName, getMethodName(),
                    useWrapper ? (", " + ModelUtils.getWrapper()) : "");
        } else {
            codeBuilder.beginControlFlow("for ($T value: $L) ", loopClass, getMethodName());
            codeBuilder.addStatement("value.$L($L)", methodName, useWrapper ? ModelUtils.getWrapper() : "");
            codeBuilder.endControlFlow();
        }

        codeBuilder.endControlFlow();
    }

    private String getMethodName() {
        return String.format("%1s.%1s()", ModelUtils.getVariable(), methodName);
    }

    private String getVariableName() {
        return String.format("%1s.%1s", ModelUtils.getVariable(), variableName);
    }

}
