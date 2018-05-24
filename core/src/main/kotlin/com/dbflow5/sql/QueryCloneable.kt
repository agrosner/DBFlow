package com.dbflow5.sql

/**
 * Description:
 */
interface QueryCloneable<out T> {

    fun cloneSelf(): T
}