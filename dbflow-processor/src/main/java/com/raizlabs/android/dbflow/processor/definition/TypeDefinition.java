package com.raizlabs.android.dbflow.processor.definition;

import com.squareup.javapoet.TypeSpec;

/**
 * Description: Simple interface for returning a {@link TypeSpec}.
 */
public interface TypeDefinition {

    /**
     * @return The {@link TypeSpec} used to write this class' type file.
     */
    TypeSpec getTypeSpec();
}

