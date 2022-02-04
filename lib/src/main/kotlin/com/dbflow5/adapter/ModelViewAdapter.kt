package com.dbflow5.adapter

/**
 * Description: The base class for a [T] adapter that defines how it interacts with the DB.
 */
abstract class ModelViewAdapter<T : Any>
    : SQLObjectAdapter<T>()
