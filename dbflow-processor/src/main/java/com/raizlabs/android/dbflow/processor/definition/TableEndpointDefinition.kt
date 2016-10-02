package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.annotation.provider.Notify
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

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

    var notifyDefinitionPathMap: MutableMap<String, MutableMap<Notify.Method, MutableList<NotifyDefinition>>>
            = mutableMapOf()

    var tableName: String? = null

    var contentProviderName: TypeName? = null

    var isTopLevel = false

    init {

        val endpoint = typeElement.getAnnotation(TableEndpoint::class.java)
        if (endpoint != null) {

            tableName = endpoint.name

            try {
                endpoint.contentProvider
            } catch (mte: MirroredTypeException) {
                contentProviderName = TypeName.get(mte.typeMirror)
            }

        }

        isTopLevel = typeElement.enclosingElement is PackageElement

        val elements = processorManager.elements.getAllMembers(typeElement as TypeElement)
        for (innerElement in elements) {
            if (innerElement.getAnnotation(ContentUri::class.java) != null) {
                val contentUriDefinition = ContentUriDefinition(innerElement, processorManager)
                if (!pathValidationMap.containsKey(contentUriDefinition.path)) {
                    contentUriDefinitions.add(contentUriDefinition)
                } else {
                    processorManager.logError("There must be unique paths for the specified @ContentUri" + " %1s from %1s", contentUriDefinition.name, contentProviderName)
                }
            } else if (innerElement.getAnnotation(Notify::class.java) != null) {
                val notifyDefinition = NotifyDefinition(innerElement, processorManager)

                for (path in notifyDefinition.paths) {
                    var methodListMap = notifyDefinitionPathMap[path]
                    if (methodListMap == null) {
                        methodListMap = mutableMapOf()
                        notifyDefinitionPathMap.put(path, methodListMap)
                    }

                    var notifyDefinitionList: MutableList<NotifyDefinition>? = methodListMap[notifyDefinition.method]
                    if (notifyDefinitionList == null) {
                        notifyDefinitionList = arrayListOf()
                        methodListMap.put(notifyDefinition.method, notifyDefinitionList)
                    }
                    notifyDefinitionList!!.add(notifyDefinition)
                }
            }
        }

    }
}
