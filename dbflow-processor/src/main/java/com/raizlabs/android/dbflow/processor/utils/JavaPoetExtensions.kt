package com.raizlabs.android.dbflow.processor.utils

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass


val publicFinal = listOf(Modifier.PUBLIC, Modifier.FINAL)

fun TypeSpec.Builder.field(fieldSpec: FieldSpec.Builder,
                           fieldSpecMethod: FieldSpec.Builder.() -> FieldSpec.Builder = { this }) = fieldSpecMethod(fieldSpec).build()

fun TypeSpec.Builder.method(methodSpec: MethodSpec.Builder,
                            methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder = { this }) = methodSpecMethod(methodSpec).build()

fun TypeSpec.Builder.overrideMethod(methodSpec: MethodSpec.Builder,
                                    methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder = { this })
        = methodSpecMethod(methodSpec).addAnnotation(Override::class.java).build()

infix fun TypeName?.name(name: String?) = FieldSpec.builder(this, name)!!

infix fun String.returns(typeName: TypeName?) = MethodSpec.methodBuilder(this).returns(typeName)!!

infix fun String.returns(kClass: KClass<*>) = MethodSpec.methodBuilder(this).returns(ClassName.get(kClass.java))!!


fun <T : Any> FieldSpec.Builder.annotation(kClass: KClass<T>, name: String = "", value: String = "")
        = addAnnotation(AnnotationSpec.builder(kClass.java).addMember(name, value).build())!!


fun <T : Any> MethodSpec.Builder.annotation(kClass: KClass<T>, name: String = "", value: String = "")
        = addAnnotation(AnnotationSpec.builder(kClass.java).addMember(name, value).build())!!

infix fun MethodSpec.Builder.modifiers(list: List<Modifier>) = addModifiers(list)