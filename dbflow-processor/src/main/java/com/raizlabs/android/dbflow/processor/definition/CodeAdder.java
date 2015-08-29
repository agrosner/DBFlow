package com.raizlabs.android.dbflow.processor.definition;

import com.squareup.javapoet.CodeBlock;

/**
 * Description:
 */
public interface CodeAdder {

    void addCode(CodeBlock.Builder code);
}
