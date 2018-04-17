package com.raizlabs.dbflow5.sql

import com.raizlabs.dbflow5.data.Blob

actual val platformTypeMap: Map<String, SQLiteType> = hashMapOf(
    ByteArray::class.java.name to SQLiteType.BLOB,
    Byte::class.java.name to SQLiteType.INTEGER,
    Short::class.java.name to SQLiteType.INTEGER,
    Int::class.java.name to SQLiteType.INTEGER,
    Long::class.java.name to SQLiteType.INTEGER,
    Float::class.java.name to SQLiteType.REAL,
    Double::class.java.name to SQLiteType.REAL,
    Boolean::class.java.name to SQLiteType.INTEGER,
    Char::class.java.name to SQLiteType.TEXT,
    CharSequence::class.java.name to SQLiteType.TEXT,
    String::class.java.name to SQLiteType.TEXT,
    Array<Byte>::class.java.name to SQLiteType.BLOB,
    Blob::class.java.name to SQLiteType.BLOB,
    Byte::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
    Short::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
    Int::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
    Long::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
    Float::class.javaPrimitiveType!!.name to SQLiteType.REAL,
    Double::class.javaPrimitiveType!!.name to SQLiteType.REAL,
    Boolean::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
    Char::class.javaPrimitiveType!!.name to SQLiteType.TEXT
)
