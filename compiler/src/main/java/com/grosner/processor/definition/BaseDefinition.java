package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class BaseDefinition implements Definition, FlowWriter {

    public final ProcessorManager manager;

    public final String elementClassName;

    public final Element element;

    public String definitionClassName;

    public String packageName;

    public BaseDefinition(Element typeElement, ProcessorManager processorManager) {
        this.manager = processorManager;
        this.element = typeElement;
        elementClassName = element.getSimpleName().toString();
        packageName = manager.getElements().getPackageOf(typeElement).toString();
    }

    protected void setDefinitionClassName(String definitionClassName) {
        this.definitionClassName = elementClassName + definitionClassName;
    }

    @Override
    public String getSourceFileName() {
        return packageName + "." + definitionClassName;
    }

    public ProcessorManager getManager() {
        return manager;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitPackage(packageName);
        javaWriter.emitImports(getImports());
        javaWriter.beginType(definitionClassName, "class" , Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), getExtendsClass(), getImplementsClasses());
        onWriteDefinition(javaWriter);
        javaWriter.endType();
        javaWriter.close();
    }

    protected String[] getImports() {
        return new String[0];
    }

    protected String getExtendsClass() {
        return null;
    }

    protected String[] getImplementsClasses() {
        return new String[0];
    }

    public void onWriteDefinition(JavaWriter javaWriter) throws IOException {

    }
}
