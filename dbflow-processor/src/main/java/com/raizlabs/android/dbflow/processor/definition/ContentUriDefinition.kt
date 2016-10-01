package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.provider.ContentUri
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.ClassName

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class ContentUriDefinition(typeElement: Element, processorManager: ProcessorManager)
: BaseDefinition(typeElement, processorManager) {

    var name: String

    var path: String

    var type: String

    var queryEnabled: Boolean = false

    var insertEnabled: Boolean = false

    var deleteEnabled: Boolean = false

    var updateEnabled: Boolean = false

    var segments: Array<ContentUri.PathSegment>

    init {

        val contentUri = typeElement.getAnnotation(ContentUri::class.java)

        path = contentUri.path

        type = contentUri.type

        name = typeElement.enclosingElement.simpleName.toString() + "_" + typeElement.simpleName.toString()

        queryEnabled = contentUri.queryEnabled

        insertEnabled = contentUri.insertEnabled

        deleteEnabled = contentUri.deleteEnabled

        updateEnabled = contentUri.updateEnabled

        segments = contentUri.segments

        if (typeElement is VariableElement) {
            if (ClassNames.URI != elementTypeName) {
                processorManager.logError("Content Uri field returned wrong type. It must return a Uri")
            }
        } else if (typeElement is ExecutableElement) {
            if (ClassNames.URI != elementTypeName) {
                processorManager.logError("ContentUri method returns wrong type. It must return Uri")
            }
        }
    }

    override fun getElementClassName(element: Element): ClassName? {
        return null
    }
}
