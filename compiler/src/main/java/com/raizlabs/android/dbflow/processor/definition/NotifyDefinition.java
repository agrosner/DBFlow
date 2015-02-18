package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description:
 */
public class NotifyDefinition extends BaseDefinition {

    public String[] paths;

    public Notify.Method method;

    public String parent;

    public String methodName;

    public String params;

    public boolean returnsArray;

    public boolean returnsSingle;

    public NotifyDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        Notify notify = typeElement.getAnnotation(Notify.class);

        paths = notify.paths();

        method = notify.method();

        parent = ((TypeElement) typeElement.getEnclosingElement()).getQualifiedName().toString();
        methodName = typeElement.getSimpleName().toString();

        ExecutableElement executableElement = ((ExecutableElement) typeElement);

        List<? extends VariableElement> parameters = executableElement.getParameters();
        StringBuilder paramsBuilder = new StringBuilder();
        boolean first = true;
        for (VariableElement param : parameters) {
            if (first) {
                first = false;
            } else {
                paramsBuilder.append(", ");
            }
            TypeMirror paramType = param.asType();
            String typeAsString = paramType.toString();
            if ("android.content.Context".equals(typeAsString)) {
                paramsBuilder.append("getContext()");
            } else if ("android.net.Uri".equals(typeAsString)) {
                paramsBuilder.append("uri");
            } else if ("android.content.ContentValues".equals(typeAsString)) {
                paramsBuilder.append("values");
            } else if ("long".equals(typeAsString)) {
                paramsBuilder.append("id");
            } else if ("java.lang.String".equals(typeAsString)) {
                paramsBuilder.append("where");
            } else if ("java.lang.String[]".equals(typeAsString)) {
                paramsBuilder.append("whereArgs");
            }
        }

        params = paramsBuilder.toString();

        TypeMirror typeMirror = executableElement.getReturnType();
        if ((Classes.URI + "[]").equals(typeMirror.toString())) {
            returnsArray = true;
        } else if (Classes.URI.equals(typeMirror.toString())) {
            returnsSingle = true;
        } else {
            processorManager.logError("Notify method returns wrong type. It must return Uri or Uri[]");
        }
    }
}
