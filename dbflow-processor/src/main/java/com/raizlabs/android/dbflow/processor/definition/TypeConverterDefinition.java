package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Description: Holds data about type converters in order to write them.
 */
public class TypeConverterDefinition {

    private ClassName className;

    private TypeName modelTypeName;

    private TypeName dbTypeName;

    public TypeConverterDefinition(TypeElement className, ProcessorManager manager) {

        this.className = ClassName.get(className);

        Types types = manager.getTypeUtils();

        DeclaredType typeConverterSuper = null;
        DeclaredType typeConverter = manager.getTypeUtils().getDeclaredType(manager.getElements().getTypeElement(ClassNames.TYPE_CONVERTER.toString()),
                types.getWildcardType(null, null), types.getWildcardType(null, null));

        for (TypeMirror superType : types.directSupertypes(className.asType())) {
            if (types.isAssignable(superType, typeConverter)) {
                typeConverterSuper = (DeclaredType) superType;
            }
        }

        if (typeConverterSuper != null) {
            List<? extends TypeMirror> typeArgs = typeConverterSuper.getTypeArguments();
            dbTypeName = ClassName.get(typeArgs.get(0));
            modelTypeName = ClassName.get(typeArgs.get(1));
        }
    }

    public TypeName getModelTypeName() {
        return modelTypeName;
    }

    public TypeName getDbTypeName() {
        return dbTypeName;
    }

    public ClassName getClassName() {
        return className;
    }

}
