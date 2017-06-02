package com.raizlabs.android.dbflow.list;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;

/**
 * Description: Provides iteration capabilities to a {@link FlowCursorList}.
 */
public class FlowCursorIterator<TModel> implements ListIterator<TModel>, AutoCloseable {

    private final IFlowCursorIterator<TModel> cursorList;
    private long reverseIndex;
    private long startingCount;
    private long count;

    public FlowCursorIterator(@NonNull IFlowCursorIterator<TModel> cursorList) {
        this(cursorList, 0, cursorList.getCount());
    }

    public FlowCursorIterator(@NonNull IFlowCursorIterator<TModel> cursorList, int startingLocation) {
        this(cursorList, startingLocation, cursorList.getCount() - startingLocation);
    }

    public FlowCursorIterator(@NonNull IFlowCursorIterator<TModel> cursorList, int startingLocation,
                              long count) {
        this.cursorList = cursorList;
        this.count = count;
        Cursor cursor = cursorList.cursor();
        if (cursor != null) {
            // request larger than actual count.
            if (this.count > cursor.getCount() - startingLocation) {
                this.count = cursor.getCount() - startingLocation;
            }

            cursor.moveToPosition(startingLocation - 1);
            startingCount = cursorList.getCount();
            reverseIndex = this.count;
            reverseIndex -= startingLocation;

            if (reverseIndex < 0) {
                reverseIndex = 0;
            }
        }
    }

    @Override
    public void close() throws Exception {
        cursorList.close();
    }

    @Override
    public void add(@Nullable TModel object) {
        throw new UnsupportedOperationException("Cursor Iterator: Cannot add a model in the iterator");
    }

    @Override
    public boolean hasNext() {
        checkSizes();
        return reverseIndex > 0;
    }

    @Override
    public boolean hasPrevious() {
        checkSizes();
        return reverseIndex < count;
    }

    @Nullable
    @Override
    public TModel next() {
        checkSizes();
        TModel item = cursorList.getItem(count - reverseIndex);
        reverseIndex--;
        return item;
    }

    @Override
    public int nextIndex() {
        return (int) (reverseIndex + 1);
    }

    @Nullable
    @Override
    public TModel previous() {
        checkSizes();
        TModel item = cursorList.getItem(count - reverseIndex);
        reverseIndex++;
        return item;
    }

    @Override
    public int previousIndex() {
        return (int) reverseIndex;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cursor Iterator: cannot remove from an active Iterator ");
    }

    @Override
    public void set(@Nullable TModel object) {
        throw new UnsupportedOperationException("Cursor Iterator: cannot set on an active Iterator ");
    }

    private void checkSizes() {
        if (startingCount != cursorList.getCount()) {
            throw new ConcurrentModificationException("Cannot change Cursor data during iteration.");
        }
    }
}
