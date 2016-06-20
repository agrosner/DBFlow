package com.raizlabs.android.dbflow.list;

import android.database.Cursor;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.Iterator;

/**
 * Description:
 */
public class CursorIterator<TModel extends Model> implements Iterator<TModel> {

    private final FlowCursorList<TModel> cursorList;
    private int count;

    public CursorIterator(FlowCursorList<TModel> cursorList) {
        this.cursorList = cursorList;
        Cursor cursor = cursorList.cursor();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            count = cursor.getCount();
        }
    }

    @Override
    public boolean hasNext() {
        return count > 0;
    }

    @Override
    public TModel next() {
        TModel item = cursorList.getItem(cursorList.getCount() - count);
        count--;
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cursor Iterator: cannot remove from an active Iterator ");
    }
}
