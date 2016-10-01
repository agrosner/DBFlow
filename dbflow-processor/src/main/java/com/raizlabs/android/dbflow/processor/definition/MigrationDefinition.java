package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Used in holding data about migration files.
 */
public class MigrationDefinition extends BaseDefinition {


    public TypeName databaseName;

    public Integer version;

    public int priority = -1;

    private String constructorName;

    public MigrationDefinition(ProcessorManager processorManager, TypeElement typeElement) {
        super(typeElement, processorManager);
        setOutputClassName("");

        Migration migration = typeElement.getAnnotation(Migration.class);
        if (migration == null) {
            processorManager.logError("Migration was null for:" + typeElement);
        } else {
            try {
                migration.database();
            } catch (MirroredTypeException mte) {
                databaseName = TypeName.get(mte.getTypeMirror());
            }
            version = migration.version();
            priority = migration.priority();

            List<? extends Element> elements = typeElement.getEnclosedElements();
            for (Element element : elements) {
                if (element instanceof ExecutableElement && element.getSimpleName().toString().equals("<init>")) {
                    if (!StringUtils.isNullOrEmpty(constructorName)) {
                        getManager().logError(MigrationDefinition.class, "Migrations cannot have more than one constructor. " +
                            "They can only have an Empty() or single-parameter constructor Empty(Empty.class) that specifies " +
                            "the .class of this migration class.");
                    }

                    if (((ExecutableElement) element).getParameters().isEmpty()) {
                        constructorName = "()";
                    } else if (((ExecutableElement) element).getParameters().size() == 1) {
                        List<? extends Element> params = ((ExecutableElement) element).getParameters();
                        Element param = params.get(0);

                        TypeName type = TypeName.get(param.asType());
                        if (type instanceof ParameterizedTypeName &&
                            ((ParameterizedTypeName) type).rawType.equals(ClassName.get(Class.class))) {
                            TypeName containedType = ((ParameterizedTypeName) type).typeArguments.get(0);
                            constructorName = CodeBlock.builder().add("($T.class)", containedType).build().toString();
                        } else {
                            getManager().logError(MigrationDefinition.class, "Wrong parameter type found for %1s. Found %1s" +
                                "but required ModelClass.class", typeElement, type);
                        }
                    }
                }
            }
        }
    }

    public String getConstructorName() {
        return constructorName;
    }

}
