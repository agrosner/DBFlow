package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TypeConverterDefinition {

    private TypeElement classElement;

    private TypeElement modelElement;

    private TypeElement dbElement;

    public TypeConverterDefinition(TypeElement classElement, ProcessorManager manager) {

        this.classElement = classElement;

        Types types = manager.getTypeUtils();

        DeclaredType typeConverterSuper = null;
        DeclaredType typeConverter = manager.getTypeUtils().getDeclaredType(manager.getElements().getTypeElement(Classes.TYPE_CONVERTER),
                types.getWildcardType(null, null), types.getWildcardType(null, null));

        for(TypeMirror superType: types.directSupertypes(classElement.asType())) {
            if(types.isAssignable(superType, typeConverter)) {
                typeConverterSuper = (DeclaredType) superType;
            }
        }

        if(typeConverterSuper != null) {
            List<? extends TypeMirror> typeArgs = typeConverterSuper.getTypeArguments();
            dbElement = manager.getElements().getTypeElement(typeArgs.get(0).toString());
            modelElement = manager.getElements().getTypeElement(typeArgs.get(1).toString());
        }
    }

    public TypeElement getModelElement() {
        return modelElement;
    }

    public TypeElement getDbElement() {
        return dbElement;
    }

    public TypeElement getClassElement() {
        return classElement;
    }

    public String getModelClassQualifiedName() {
        return modelElement.getQualifiedName().toString();
    }

    public String getQualifiedName() {
        return classElement.getQualifiedName().toString();
    }
}
