package com.grosner.processor;

import com.google.auto.service.AutoService;
import com.grosner.dbflow.annotation.*;
import com.grosner.processor.handler.*;
import com.grosner.processor.model.ProcessorManager;

import java.util.ArrayList;
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
 */
@AutoService(Processor.class)
public class DBFlowProcessor extends AbstractProcessor {

    public static String DEFAULT_DB_NAME;

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
        supportedTypes.add(TypeConverter.class.getName());
        supportedTypes.add(ContainerAdapter.class.getName());
        supportedTypes.add(ModelView.class.getName());
        supportedTypes.add(Migration.class.getName());
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
        manager.addHandlers(
                new MigrationHandler(),
                new TypeConverterHandler(),
                new TableHandler(),
                new ModelContainerHandler(),
                new ModelViewHandler(),
                new FlowManagerHandler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        ArrayList<Element> elements = new ArrayList<Element>(roundEnv.getElementsAnnotatedWith(Database.class));
        if(elements.size() > 0) {
            Database database = elements.get(0).getAnnotation(Database.class);
            DEFAULT_DB_NAME = database.name();
        }
        manager.handle(manager, roundEnv);

        // return true if we successfully processed the Annotation.
        return true;
    }

}
