package com.dbflow5.processor.definition.provider

import com.dbflow5.contentprovider.annotation.ContentUri
import com.dbflow5.contentprovider.annotation.Notify
import com.dbflow5.contentprovider.annotation.NotifyMethod
import com.dbflow5.contentprovider.annotation.TableEndpoint
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class TableEndpointDefinition(tableEndpoint: TableEndpoint,
                              typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var contentUriDefinitions: MutableList<ContentUriDefinition> = mutableListOf()

    /**
     * Dont want duplicate paths.
     */
    internal var pathValidationMap: Map<String, ContentUriDefinition> = mutableMapOf()

    var notifyDefinitionPathMap: MutableMap<String, MutableMap<NotifyMethod, MutableList<NotifyDefinition>>> = mutableMapOf()

    var tableName: String? = null

    var contentProviderName: TypeName? = null

    var isTopLevel = false

    init {
        contentProviderName = tableEndpoint.extractTypeNameFromAnnotation {
            tableName = it.name
            it.contentProvider
        }

        isTopLevel = typeElement.enclosingElement is PackageElement

        val elements = processorManager.elements.getAllMembers(typeElement as TypeElement)
        for (innerElement in elements) {
            innerElement.annotation<ContentUri>()?.let { contentUri ->
                val contentUriDefinition = ContentUriDefinition(contentUri, innerElement, processorManager)
                if (!pathValidationMap.containsKey(contentUriDefinition.path)) {
                    contentUriDefinitions.add(contentUriDefinition)
                } else {
                    processorManager.logError("There must be unique paths for the specified @ContentUri" + " %1s from %1s", contentUriDefinition.name, contentProviderName)
                }
            }
            innerElement.annotation<Notify>()?.let { notify ->
                if (innerElement is ExecutableElement) {
                    val notifyDefinition = NotifyDefinition(notify, innerElement, processorManager)
                    @Suppress("LoopToCallChain")
                    for (path in notifyDefinition.paths) {
                        val methodListMap = notifyDefinitionPathMap.getOrPut(path) { mutableMapOf() }
                        val notifyDefinitionList = methodListMap.getOrPut(notifyDefinition.method) { arrayListOf() }
                        notifyDefinitionList.add(notifyDefinition)
                    }
                }
            }
        }

    }
}
