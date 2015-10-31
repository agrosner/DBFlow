package com.raizlabs.android.dbflow.processor.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.raizlabs.android.dbflow.annotation.provider.ContentUri;
import com.raizlabs.android.dbflow.annotation.provider.Notify;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Description:
 */
public class TableEndpointDefinition extends BaseDefinition {

    public List<ContentUriDefinition> contentUriDefinitions = Lists.newArrayList();

    /**
     * Dont want duplicate paths.
     */
    Map<String, ContentUriDefinition> pathValidationMap = Maps.newHashMap();

    public Map<String, Map<Notify.Method, List<NotifyDefinition>>> notifyDefinitionPathMap = Maps.newHashMap();

    public String tableName;

    public String contentProviderName;

    public boolean isTopLevel = false;

    public TableEndpointDefinition(Element typeElement, ProcessorManager processorManager) {
        super(typeElement, processorManager);

        TableEndpoint endpoint = typeElement.getAnnotation(TableEndpoint.class);

        tableName = endpoint.name();

        contentProviderName = endpoint.contentProviderName();

        isTopLevel = typeElement.getEnclosingElement() instanceof PackageElement;

        List<? extends Element> elements = processorManager.getElements().getAllMembers((TypeElement) typeElement);
        for (Element innerElement : elements) {
            if (innerElement.getAnnotation(ContentUri.class) != null) {
                ContentUriDefinition contentUriDefinition = new ContentUriDefinition(innerElement, processorManager);
                if (!pathValidationMap.containsKey(contentUriDefinition.path)) {
                    contentUriDefinitions.add(contentUriDefinition);
                } else {
                    processorManager.logError("There must be unique paths for the specified @ContentUri" +
                            " %1s from %1s", contentUriDefinition.name, contentProviderName);
                }
            } else if (innerElement.getAnnotation(Notify.class) != null) {
                NotifyDefinition notifyDefinition = new NotifyDefinition(innerElement, processorManager);

                for (String path : notifyDefinition.paths) {
                    Map<Notify.Method, List<NotifyDefinition>> methodListMap = notifyDefinitionPathMap.get(path);
                    if (methodListMap == null) {
                        methodListMap = Maps.newHashMap();
                        notifyDefinitionPathMap.put(path, methodListMap);
                    }

                    List<NotifyDefinition> notifyDefinitionList = methodListMap.get(notifyDefinition.method);
                    if (notifyDefinitionList == null) {
                        notifyDefinitionList = Lists.newArrayList();
                        methodListMap.put(notifyDefinition.method, notifyDefinitionList);
                    }
                    notifyDefinitionList.add(notifyDefinition);
                }
            }
        }

    }
}
