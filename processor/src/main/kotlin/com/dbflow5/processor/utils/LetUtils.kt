package com.dbflow5.processor.utils

/**
 * Description: Multi-let execution.
 */
inline fun <A, B, R> safeLet(a: A?, b: B?, fn: (a: A, b: B) -> R) {
    if (a != null && b != null) fn(a, b)
}