package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Description: Represents the {@link OneToMany} annotation.
 */
public class OneToManyDefinition extends BaseDefinition {

    public String methodName;

    public String variableName;

    public List<OneToMany.Method> methods = Lists.newArrayList();

    public OneToManyDefinition(Element typeElement,
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
     * Writes the method to the specified java writer for loading from DB.
     *
     * @param javaWriter
     * @throws IOException
     */
    public void writeLoad(JavaWriter javaWriter) throws IOException {
        if(isLoad()) {
            javaWriter.emitStatement(getMethodName());
        }
    }

    /**
     * Writes a delete method that will delete all related objects.
     *
     * @param javaWriter
     * @throws IOException
     */
    public void writeDelete(JavaWriter javaWriter) throws IOException {
        if(isDelete()) {
            javaWriter.emitStatement("new %1s<>(%1s.withModels(%1s)).onExecute()",
                                     Classes.DELETE_MODEL_LIST_TRANSACTION,
                                     Classes.PROCESS_MODEL_INFO, getMethodName());
        }
    }

    public void writeSave(JavaWriter javaWriter) throws IOException {
        if (isSave()) {
            javaWriter.emitStatement("new %1s<>(%1s.withModels(%1s)).onExecute()",
                                    Classes.SAVE_MODEL_LIST_TRANSACTION,
                                    Classes.PROCESS_MODEL_INFO, getMethodName());
        }
    }


    private String getMethodName() {
        return String.format("%1s.%1s()", ModelUtils.getVariable(false), methodName);
    }

    private String getVariableName() {
        return String.format("%1s.%1s", ModelUtils.getVariable(false), variableName);
    }

}
