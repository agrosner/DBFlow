package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Description:
 */
public class TableEndpointDefinition extends BaseDefinition {

    public List<ContentUriDefinition> contentUriDefinitions = Lists.newArrayList();

    public String tableName;

    public TableEndpointDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        TableEndpoint endpoint = typeElement.getAnnotation(TableEndpoint.class);

        tableName = endpoint.value();

        List<? extends Element> elements = processorManager.getElements().getAllMembers((TypeElement) typeElement);
        for(Element innerElement: elements) {
            if(innerElement.getAnnotation(ContentUri.class) != null) {
                ContentUriDefinition contentUriDefinition = new ContentUriDefinition(innerElement, processorManager);

                contentUriDefinitions.add(contentUriDefinition);
            }
        }
    }
}
