package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeAdder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes out the custom type converter fields.
 */
public class CustomTypeConverterPropertyMethod implements TypeAdder {

    private final BaseTableDefinition baseTableDefinition;

    public CustomTypeConverterPropertyMethod(BaseTableDefinition baseTableDefinition) {
        this.baseTableDefinition = baseTableDefinition;
    }


    @Override
    public void addToType(TypeSpec.Builder typeBuilder) {
        Set<ClassName> customTypeConverters = baseTableDefinition.getAssociatedTypeConverters().keySet();
        for (ClassName className : customTypeConverters) {
            typeBuilder.addField(FieldSpec.builder(className, "typeConverter" + className.simpleName().toString(), Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $()", className)
                    .build());
        }
    }
}
