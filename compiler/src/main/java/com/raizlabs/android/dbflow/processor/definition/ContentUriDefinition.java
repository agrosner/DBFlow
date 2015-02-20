package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Description:
 */
public class ContentUriDefinition extends BaseDefinition {

    public String name;

    public String classQualifiedName;

    public String path;

    public String type;

    public boolean queryEnabled;

    public boolean insertEnabled;

    public boolean deleteEnabled;

    public boolean updateEnabled;

    public ContentUri.PathSegment[] segments;

    public ContentUriDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        ContentUri contentUri = typeElement.getAnnotation(ContentUri.class);

        path = contentUri.path();

        type = contentUri.type();

        name = typeElement.getEnclosingElement().getSimpleName().toString()
                + "_" + typeElement.getSimpleName().toString();

        queryEnabled = contentUri.queryEnabled();

        insertEnabled = contentUri.insertEnabled();

        deleteEnabled = contentUri.deleteEnabled();

        updateEnabled = contentUri.updateEnabled();

        segments = contentUri.segments();

        if(typeElement instanceof VariableElement) {
            TypeMirror typeMirror = typeElement.asType();
            if (!Classes.URI.equals(typeMirror.toString())) {
                processorManager.logError("Content Uri field returned wrong type. It must return a Uri");
            }
        } else if(typeElement instanceof ExecutableElement) {
            TypeMirror typeMirror = ((ExecutableElement) typeElement).getReturnType();
            if (!Classes.URI.equals(typeMirror.toString())) {
                processorManager.logError("ContentUri method returns wrong type. It must return Uri");
            }
        }
    }
}
