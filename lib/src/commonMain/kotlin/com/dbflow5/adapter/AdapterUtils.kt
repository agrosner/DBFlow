package com.dbflow5.adapter

/**
 * Used for code gen only.
 */
inline fun <T1 : Any, R> dbFlowInternalSafeLet(t1: T1?, fn: (T1) -> R) = t1?.let(fn)

inline fun <T1 : Any, T2 : Any, R : Any> dbFlowInternalSafeLet(
    p1: T1?,
    p2: T2?,
    block: (T1, T2) -> R?
): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> dbFlowInternalSafeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> dbFlowInternalSafeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> dbFlowInternalSafeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(
        p1,
        p2,
        p3,
        p4,
        p5
    ) else null
}

inline fun <T1 : Any, R> dbFlowInternalLet(t1: T1, fn: (T1) -> R) = t1.let(fn)

inline fun <T1, T2, R> dbFlowInternalLet(
    p1: T1,
    p2: T2,
    block: (T1, T2) -> R
): R = block(p1, p2)

inline fun <T1, T2, T3, R> dbFlowInternalLet(
    p1: T1,
    p2: T2,
    p3: T3,
    block: (T1, T2, T3) -> R
): R = block(p1, p2, p3)

inline fun <T1, T2, T3, T4, R> dbFlowInternalLet(
    p1: T1,
    p2: T2,
    p3: T3,
    p4: T4,
    block: (T1, T2, T3, T4) -> R
): R = block(p1, p2, p3, p4)

inline fun <T1, T2, T3, T4, T5, R> dbFlowInternalLet(
    p1: T1,
    p2: T2,
    p3: T3,
    p4: T4,
    p5: T5,
    block: (T1, T2, T3, T4, T5) -> R
): R = block(
    p1,
    p2,
    p3,
    p4,
    p5
)