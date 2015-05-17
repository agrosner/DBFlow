package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.processor.definition.BaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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

    /**
     * Checks if className is valid Java class name. It will check for Java keywords like abstract, assert, boolean etc.
     * and it will check if className matches regex pattern [A-Za-z_$]+[a-zA-Z0-9_$]*
     *
     * @param className class name to validate without package name.
     * @return {@code true} if parameter is a valid Java class name, {@code false} otherwise.
     */
    public static boolean isValidJavaClassName(final String className) {
        final List<String> javaKeywords = Arrays.asList(
                "abstract", "assert", "boolean", "break", "byte",
                "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else",
                "enum", "extends", "false", "final", "finally",
                "float", "for", "goto", "if", "implements",
                "import", "instanceof", "int", "interface", "long",
                "native", "new", "null", "package", "private",
                "protected", "public", "return", "short", "static",
                "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "true", "try",
                "void", "volatile", "while");

        final Pattern javaClassNamePattern = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*");

        return !javaKeywords.contains(className) && javaClassNamePattern.matcher(className).matches();
    }
}
