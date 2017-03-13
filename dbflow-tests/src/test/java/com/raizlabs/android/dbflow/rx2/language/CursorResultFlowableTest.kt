@file:Suppress("UNCHECKED_CAST")

package com.raizlabs.android.dbflow.rx2.language

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.kotlinextensions.cursorResult
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.structure.TestModel1
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.reactivestreams.Subscriber

/**
 * Description:
 */
class CursorResultFlowableTest : FlowTestCase() {

    @Before
    fun setupTest() {

        (0..9).forEach {
            TestModel1().apply {
                name = it.toString()
            }.save()
        }

    }

    @Test
    fun testIterateMax() {
        val subscriberMock = mock<Subscriber<TestModel1>>()
        val disposableMock = mock<Disposable>()
        val cursorResult = CursorResultFlowable.CursorResultObserver<TestModel1>(subscriberMock, Long.MAX_VALUE)
        cursorResult.onSubscribe(disposableMock)

        cursorResult.onSuccess((select from TestModel1::class).cursorResult)


        val nextCaptor = argumentCaptor<TestModel1>()
        verify(subscriberMock, times(10)).onNext(nextCaptor.capture())
    }

    @Test
    fun testIteratePieces() {
        val subscriberMock = mock<Subscriber<TestModel1>>()
        val disposableMock = mock<Disposable>()
        val cursorResult = CursorResultFlowable.CursorResultObserver<TestModel1>(subscriberMock, 5)
        cursorResult.onSubscribe(disposableMock)

        cursorResult.onSuccess((select from TestModel1::class).cursorResult)


        val nextCaptor = argumentCaptor<TestModel1>()
        verify(subscriberMock, times(5)).onNext(nextCaptor.capture())

        cursorResult.onSuccess((select from TestModel1::class).cursorResult)
        verify(subscriberMock, times(10)).onNext(nextCaptor.capture())
    }
}