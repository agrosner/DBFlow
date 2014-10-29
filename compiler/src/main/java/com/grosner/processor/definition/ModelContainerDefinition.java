package com.grosner.processor.definition;

import com.grosner.processor.Classes;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelContainerDefinition implements FlowWriter {

    public static final String DBFLOW_MODEL_CONTAINER_TAG = "$Container";

    private final TypeElement classElement;

    private final ProcessorManager manager;

    private TypeElement modelElement;

    private TypeElement dataElement;

    private String sourceFileName;

    private String packageName;

    public ModelContainerDefinition(String packageName, TypeElement classElement, ProcessorManager manager) {

        this.classElement = classElement;
        this.manager = manager;
        this.sourceFileName = classElement.getSimpleName().toString() + DBFLOW_MODEL_CONTAINER_TAG;
        this.packageName = packageName;

        Types types = manager.getTypeUtils();

        DeclaredType ModelContainerSuper = null;
        DeclaredType modelContainer = manager.getTypeUtils().getDeclaredType(manager.getElements().getTypeElement(Classes.MODEL_CONTAINER),
                types.getWildcardType(manager.getElements().getTypeElement(Classes.MODEL).asType(), null),
                types.getWildcardType(null, null));

        for(TypeMirror superType: types.directSupertypes(classElement.asType())) {
            if(types.isAssignable(superType, modelContainer)) {
                ModelContainerSuper = (DeclaredType) superType;
            }
        }

        if(ModelContainerSuper != null) {
            List<? extends TypeMirror> typeArgs = ModelContainerSuper.getTypeArguments();
            dataElement = manager.getElements().getTypeElement(typeArgs.get(1).toString());
            modelElement = manager.getElements().getTypeElement(typeArgs.get(0).toString());
        }
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        
    }

    public String getModelClassQualifiedName() {
        return modelElement.getQualifiedName().toString();
    }

    public String getFQCN() {
        return packageName+ "." + sourceFileName;
    }

}
