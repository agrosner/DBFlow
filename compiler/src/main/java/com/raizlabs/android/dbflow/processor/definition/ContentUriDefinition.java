package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.Element;

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

    }
}
