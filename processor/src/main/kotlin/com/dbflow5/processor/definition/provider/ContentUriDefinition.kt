package com.dbflow5.processor.definition.provider

import com.dbflow5.contentprovider.annotation.ContentUri
import com.dbflow5.processor.ClassNames
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

/**
 * Description:
 */
class ContentUriDefinition(contentUri: ContentUri,
                           typeElement: Element, processorManager: ProcessorManager)
    : BaseDefinition(typeElement, processorManager) {

    var name = "${typeElement.enclosingElement.simpleName}_${typeElement.simpleName}"

    val path: String = contentUri.path
    val type: String = contentUri.type
    val queryEnabled: Boolean = contentUri.queryEnabled
    val insertEnabled: Boolean = contentUri.insertEnabled
    val deleteEnabled: Boolean = contentUri.deleteEnabled
    val updateEnabled: Boolean = contentUri.updateEnabled
    val segments = contentUri.segments

    init {
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
}