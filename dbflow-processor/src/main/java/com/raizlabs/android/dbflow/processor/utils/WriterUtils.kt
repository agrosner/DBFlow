package com.raizlabs.android.dbflow.processor.utils

import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.definition.BaseDefinition
import com.squareup.javapoet.JavaFile
import java.io.IOException

/**
 * Description: Provides some handy writing methods.
 */
object WriterUtils {

    fun writeBaseDefinition(baseDefinition: BaseDefinition, processorManager: ProcessorManager): Boolean {
        var success = false
        try {
            val javaFile = JavaFile.builder(baseDefinition.packageName, baseDefinition.typeSpec).build()
            javaFile.writeTo(processorManager.processingEnvironment.filer)
            success = true
        } catch (e: IOException) {
            // ignored
        }

        return success
    }

}
