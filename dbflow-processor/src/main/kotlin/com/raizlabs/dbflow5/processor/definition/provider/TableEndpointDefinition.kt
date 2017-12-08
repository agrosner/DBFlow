package com.raizlabs.dbflow5.processor.definition.provider

import com.raizlabs.dbflow5.annotation.provider.ContentUri
import com.raizlabs.dbflow5.annotation.provider.Notify
import com.raizlabs.dbflow5.annotation.provider.NotifyMethod
import com.raizlabs.dbflow5.annotation.provider.TableEndpoint
import com.raizlabs.dbflow5.processor.ProcessorManager
import com.raizlabs.dbflow5.processor.definition.BaseDefinition
import com.raizlabs.dbflow5.processor.utils.annotation
import com.raizlabs.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class TableEndpointDefinition(typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var contentUriDefinitions: MutableList<ContentUriDefinition> = mutableListOf()

    /**
     * Dont want duplicate paths.
     */
    internal var pathValidationMap: Map<String, ContentUriDefinition> = mutableMapOf()

    var notifyDefinitionPathMap: MutableMap<String, MutableMap<NotifyMethod, MutableList<NotifyDefinition>>>
            = mutableMapOf()

    var tableName: String? = null

    var contentProviderName: TypeName? = null

    var isTopLevel = false

    init {
        contentProviderName = typeElement.extractTypeNameFromAnnotation<TableEndpoint> {
            tableName = it.name
            it.contentProvider
        }

        isTopLevel = typeElement.enclosingElement is PackageElement

        val elements = processorManager.elements.getAllMembers(typeElement as TypeElement)
        for (innerElement in elements) {
            if (innerElement.annotation<ContentUri>() != null) {
                val contentUriDefinition = ContentUriDefinition(innerElement, processorManager)
                if (!pathValidationMap.containsKey(contentUriDefinition.path)) {
                    contentUriDefinitions.add(contentUriDefinition)
                } else {
                    processorManager.logError("There must be unique paths for the specified @ContentUri" + " %1s from %1s", contentUriDefinition.name, contentProviderName)
                }
            } else if (innerElement.annotation<Notify>() != null) {
                val notifyDefinition = NotifyDefinition(innerElement, processorManager)
                for (path in notifyDefinition.paths) {
                    val methodListMap = notifyDefinitionPathMap.getOrPut(path) { mutableMapOf() }
                    val notifyDefinitionList = methodListMap.getOrPut(notifyDefinition.method) { arrayListOf() }
                    notifyDefinitionList.add(notifyDefinition)
                }
            }
        }

    }
}
