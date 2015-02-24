package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Description: Provides some handy writing methods.
 */
public class WriterUtils {

    /**
     * Emits a mehod with the proper begin and end of method.
     *
     * @param javaWriter The file to write to
     * @param flowWriter The writer to write on the javawriter
     * @param returnType The return type FQCN
     * @param name       The name of the method
     * @param modifiers  The modifiers to use {@link javax.lang.model.element.Modifier}
     * @param params     The params to write
     */
    public static void emitMethod(JavaWriter javaWriter, FlowWriter flowWriter, String returnType, String name, Set<Modifier> modifiers, String... params) {
        try {
            javaWriter.beginMethod(returnType, name, modifiers, params);
            flowWriter.write(javaWriter);
            javaWriter.endMethod();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Same as {@link #emitMethod(com.squareup.javawriter.JavaWriter, com.raizlabs.android.dbflow.processor.writer.FlowWriter, String, String, java.util.Set, String...)}
     * except it also emits an {@link java.lang.Override} annotation.
     *
     * @param javaWriter The file to write to
     * @param flowWriter The writer to write on the javawriter
     * @param returnType The return type FQCN
     * @param name       The name of the method
     * @param modifiers  The modifiers to use {@link javax.lang.model.element.Modifier}
     * @param params     The params to write
     */
    public static void emitOverriddenMethod(JavaWriter javaWriter, FlowWriter flowWriter, String returnType, String name, Set<Modifier> modifiers, String... params) {
        try {
            javaWriter.emitEmptyLine().emitAnnotation(Override.class);
            emitMethod(javaWriter, flowWriter, returnType, name, modifiers, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean writeBaseDefinition(BaseDefinition baseDefinition, ProcessorManager processorManager) {
        boolean success = false;
        try {
            JavaWriter javaWriter = new JavaWriter(processorManager.getProcessingEnvironment().getFiler()
                    .createSourceFile(baseDefinition.getSourceFileName()).openWriter());
            baseDefinition.write(javaWriter);

            javaWriter.close();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

}
