package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;

/**
 * Description: Provides some handy writing methods.
 */
public class WriterUtils {

    public static boolean writeBaseDefinition(BaseDefinition baseDefinition, ProcessorManager processorManager) {
        boolean success = false;
        try {
            JavaFile javaFile = JavaFile.builder(baseDefinition.packageName, baseDefinition.getTypeSpec()).build();
            javaFile.writeTo(processorManager.getProcessingEnvironment().getFiler());
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

}
