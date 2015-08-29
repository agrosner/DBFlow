package com.raizlabs.android.dbflow.processor.definition;

import com.squareup.javapoet.TypeSpec;

/**
 * Description:
 */
public interface TypeAdder {

    void addToType(TypeSpec.Builder typeBuilder);
}
