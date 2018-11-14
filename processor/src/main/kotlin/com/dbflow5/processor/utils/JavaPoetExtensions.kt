package com.dbflow5.processor.utils

import com.grosner.kpoet.returns
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import kotlin.reflect.KClass

fun TypeSpec.Builder.`override fun`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                                    codeMethod: (MethodSpec.Builder.() -> Unit) = { }) =
    addMethod(MethodSpec
        .methodBuilder(name)
        .returns(type)
        .addParameters(params.map { it.build() }.toList())
        .addAnnotation(Override::class.java)
        .apply(codeMethod).build())

fun TypeSpec.Builder.`override fun`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                                    codeMethod: (MethodSpec.Builder.() -> Unit) = { }): TypeSpec.Builder =
    addMethod(MethodSpec
        .methodBuilder(name)
        .returns(type)
        .addParameters(params.map { it.build() }.toList())
        .addAnnotation(Override::class.java)
        .apply(codeMethod).build())

fun `override fun`(type: TypeName, name: String, vararg params: ParameterSpec.Builder,
                   codeMethod: (MethodSpec.Builder.() -> Unit) = { }): MethodSpec =
    MethodSpec
        .methodBuilder(name)
        .returns(type)
        .addParameters(params.map { it.build() }.toList())
        .addAnnotation(Override::class.java)
        .apply(codeMethod).build()

fun `override fun`(type: KClass<*>, name: String, vararg params: ParameterSpec.Builder,
                   codeMethod: (MethodSpec.Builder.() -> Unit) = { }): MethodSpec =
    MethodSpec
        .methodBuilder(name)
        .returns(type)
        .addParameters(params.map { it.build() }.toList())
        .addAnnotation(Override::class.java)
        .apply(codeMethod).build()