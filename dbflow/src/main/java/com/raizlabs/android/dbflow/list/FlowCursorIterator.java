package com.raizlabs.android.dbflow.list;

import android.database.Cursor;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;

/**
 * Description: Provides iteration capabilities to a {@link FlowCursorList}.
 */
public class FlowCursorIterator<TModel> implements ListIterator<TModel>, AutoCloseable {

    private final IFlowCursorIterator<TModel> cursorList;
    private int reverseIndex;
    private int startingCount;

    public FlowCursorIterator(IFlowCursorIterator<TModel> cursorList) {
        this(cursorList, 0);
    }

    public FlowCursorIterator(IFlowCursorIterator<TModel> cursorList, int startingLocation) {
        this.cursorList = cursorList;
        Cursor cursor = cursorList.cursor();
        if (cursor != null) {
            cursor.moveToPosition(startingLocation - 1);
            reverseIndex = startingCount = cursor.getCount();
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
    public void add(TModel object) {
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
        return reverseIndex < cursorList.getCount();
    }

    @Override
    public TModel next() {
        checkSizes();
        TModel item = cursorList.getItem(cursorList.getCount() - reverseIndex);
        reverseIndex--;
        return item;
    }

    @Override
    public int nextIndex() {
        return reverseIndex + 1;
    }

    @Override
    public TModel previous() {
        checkSizes();
        TModel item = cursorList.getItem(cursorList.getCount() - reverseIndex);
        reverseIndex++;
        return item;
    }

    @Override
    public int previousIndex() {
        return reverseIndex;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cursor Iterator: cannot remove from an active Iterator ");
    }

    @Override
    public void set(TModel object) {
        throw new UnsupportedOperationException("Cursor Iterator: cannot set on an active Iterator ");
    }

    private void checkSizes() {
        if (startingCount != cursorList.getCount()) {
            throw new ConcurrentModificationException("Cannot change Cursor data during iteration.");
        }
    }
}
