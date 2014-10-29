package com.grosner.processor;

import com.google.auto.service.AutoService;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.handler.ModelContainerHandler;
import com.grosner.processor.handler.TableHandler;
import com.grosner.processor.handler.TypeConverterHandler;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@AutoService(Processor.class)
public class DBFlowProcessor extends AbstractProcessor {

    private ProcessorManager manager;

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedAnnotationTypes}, return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.
     *
     * @return the names of the annotation types supported by this
     * processor, or an empty set if none
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<String>();
        supportedTypes.add(Table.class.getName());
        supportedTypes.add(Column.class.getName());
        return supportedTypes;
    }

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedSourceVersion}, return the source version in the
     * annotation.  If the class is not so annotated, {@link
     * javax.lang.model.SourceVersion#RELEASE_6} is returned.
     *
     * @return the latest source version supported by this processor
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        manager = new ProcessorManager(processingEnv);

    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        new TypeConverterHandler(roundEnv, manager);

        new TableHandler(roundEnv, manager);
        new ModelContainerHandler(roundEnv, manager);

        // return true if we successfully processed the Annotation.
        return true;
    }

    private void createHeader(JavaWriter javaWriter, String packageName) throws IOException {
        javaWriter.emitPackage(packageName);
    }
}
