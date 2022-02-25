package com.dbflow5.processor

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Description: The main object graph during processing. This class collects all of the
 * processor classes and writes them to the corresponding database holders.
 */
class ProcessorManager(processingEnvironment: ProcessingEnvironment) {

    companion object {
        lateinit var manager: ProcessorManager
    }

    init {
        manager = this
    }

    val typeUtils: Types = processingEnvironment.typeUtils

    val elements: Elements = processingEnvironment.elementUtils

}
