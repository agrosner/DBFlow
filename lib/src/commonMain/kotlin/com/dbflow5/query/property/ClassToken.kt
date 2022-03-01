package com.dbflow5.query.property

/**
 * Used in code generation to infer property class type
 */
inline fun <reified T : Any> classToken() = T::class
