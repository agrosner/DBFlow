package com.raizlabs.dbflow5.dbflow.processor.utils

import com.grosner.kpoet.javaFile
import com.raizlabs.dbflow5.dbflow.processor.ProcessorManager
import com.raizlabs.dbflow5.dbflow.processor.definition.BaseDefinition
import java.io.IOException

/**
 * Description: Provides some handy writing methods.
 */
object WriterUtils {

    fun writeBaseDefinition(baseDefinition: BaseDefinition, processorManager: ProcessorManager): Boolean {
        var success = false
        try {
            javaFile(baseDefinition.packageName) { baseDefinition.typeSpec }
                    .writeTo(processorManager.processingEnvironment.filer)
            success = true
        } catch (e: IOException) {
            // ignored
        } catch (i: IllegalStateException) {
            processorManager.logError(WriterUtils::class, "Found error for class:" + baseDefinition.elementName)
            processorManager.logError(WriterUtils::class, i.message)
        }

        return success
    }

}
