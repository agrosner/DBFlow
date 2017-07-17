package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.list.IFlowCursorIterator

operator fun <TModel> IFlowCursorIterator<TModel>.get(i: Long): TModel = this.getItem(i) ?: throw IndexOutOfBoundsException("Could not find item at index $i from the cursor.")

operator fun <TModel> IFlowCursorIterator<TModel>.get(i: Int): TModel = this.getItem(i.toLong()) ?: throw IndexOutOfBoundsException("Could not find item at index $i from the cursor.")


