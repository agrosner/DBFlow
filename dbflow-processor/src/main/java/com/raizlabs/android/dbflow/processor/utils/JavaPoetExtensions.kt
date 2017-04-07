package com.raizlabs.android.dbflow.processor.utils

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass


val publicFinal = listOf(Modifier.PUBLIC, Modifier.FINAL)

val privateFinal = listOf(Modifier.PRIVATE, Modifier.FINAL)

val public = listOf(Modifier.PUBLIC)

// method extensions
fun method(methodSpec: MethodSpec.Builder,
           methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder) = methodSpecMethod(methodSpec).build()!!

fun overrideMethod(methodSpec: MethodSpec.Builder,
                   methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder) = methodSpecMethod(methodSpec).addAnnotation(Override::class.java).build()!!

fun TypeSpec.Builder.method(methodSpec: MethodSpec.Builder,
                            methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder = { this }) = addMethod(methodSpecMethod(methodSpec).build())!!

fun TypeSpec.Builder.constructor(methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder = { this }) = addMethod(methodSpecMethod(MethodSpec.constructorBuilder()).build())!!


fun TypeSpec.Builder.overrideMethod(methodSpec: MethodSpec.Builder,
                                    methodSpecMethod: MethodSpec.Builder.() -> MethodSpec.Builder = { this })
    = addMethod(methodSpecMethod(methodSpec).addAnnotation(Override::class.java).build())!!


// field extensions
fun TypeSpec.Builder.field(fieldSpec: FieldSpec.Builder,
                           fieldSpecMethod: FieldSpec.Builder.() -> FieldSpec.Builder = { this }) = addField(fieldSpecMethod(fieldSpec).build())!!

infix fun TypeName?.name(name: String?) = FieldSpec.builder(this, name)!!


infix fun String.returns(typeName: TypeName?) = MethodSpec.methodBuilder(this).returns(typeName)!!

infix fun String.returns(kClass: KClass<*>) = MethodSpec.methodBuilder(this).returns(ClassName.get(kClass.java))!!


fun <T : Any> FieldSpec.Builder.annotation(kClass: KClass<T>, name: String = "", value: String = "")
    = addAnnotation(AnnotationSpec.builder(kClass.java).addMember(name, value).build())!!


fun <T : Any> MethodSpec.Builder.annotation(kClass: KClass<T>, name: String = "", value: String = "")
    = addAnnotation(AnnotationSpec.builder(kClass.java).addMember(name, value).build())!!

infix fun MethodSpec.Builder.modifiers(list: List<Modifier>) = addModifiers(list)!!

infix fun String.modifiers(modifier: Array<Modifier>) = MethodSpec.methodBuilder(this).addModifiers(*modifier)!!

fun MethodSpec.Builder.modifiers(vararg modifier: Modifier) = addModifiers(*modifier)!!


infix fun FieldSpec.Builder.modifiers(list: List<Modifier>) = addModifiers(*list.toTypedArray())!!


fun MethodSpec.Builder.code(codeFunction: CodeBlock.Builder.() -> CodeBlock.Builder) = addCode(codeFunction(CodeBlock.builder()).build())!!