package com.dbflow5.processor.utils

import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.BaseDefinition
import com.grosner.kpoet.javaFile
import java.io.IOException

fun BaseDefinition.writeBaseDefinition(processorManager: ProcessorManager): Boolean {
    var success = false
    try {
        javaFile(packageName) { typeSpec }
                .writeTo(processorManager.processingEnvironment.filer)
        success = true
    } catch (e: IOException) {
        // ignored
        processorManager.logWarning(e.toString())
    } catch (i: IllegalStateException) {
        processorManager.logError(this::class, "Found error for class: $elementName")
        processorManager.logError(this::class, i.message)
    }

    return success
}