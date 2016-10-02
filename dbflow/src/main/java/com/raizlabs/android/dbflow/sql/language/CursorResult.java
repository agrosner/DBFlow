package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;

import java.io.Closeable;
import java.util.List;

/**
 * Description: A class that contains a {@link Cursor} and handy methods for retrieving data from it.
 * You must close this object post use via {@link #close()}.
 */
public class CursorResult<TModel> implements Closeable {

    private final InstanceAdapter<TModel> retrievalAdapter;

    @Nullable
    private Cursor cursor;

    @SuppressWarnings("unchecked")
    CursorResult(Class<TModel> modelClass, @Nullable Cursor cursor) {
        this.cursor = cursor;
        retrievalAdapter = FlowManager.getInstanceAdapter(modelClass);
    }

    /**
     * Swaps the current cursor and will close existing one.
     */
    public void swapCursor(@Nullable Cursor cursor) {
        if (this.cursor != null) {
            if (!this.cursor.isClosed()) {
                this.cursor.close();
            }
        }
        this.cursor = cursor;
    }

    /**
     * @return A {@link List} of items from this object. You must call {@link #close()} when finished.
     */
    @Nullable
    public List<TModel> toList() {
        if (cursor != null) {
            return retrievalAdapter.getListModelLoader().convertToData(cursor, null);
        } else {
            return null;
        }
    }

    /**
     * @return Converts the {@link Cursor} to a {@link List} of {@link TModel} and then closes it.
     */
    @Nullable
    public List<TModel> toListClose() {
        if (cursor != null) {
            return retrievalAdapter.getListModelLoader().load(cursor);
        } else {
            return null;
        }
    }

    /**
     * @return A {@link List} of items from this object. You must call {@link #close()} when finished.
     */
    @Nullable
    public <TCustom extends BaseQueryModel> List<TCustom> toCustomList(Class<TCustom> customClass) {
        if (cursor != null) {
            return FlowManager.getQueryModelAdapter(customClass)
                    .getListModelLoader().convertToData(cursor, null);
        } else {
            return null;
        }
    }

    /**
     * @return Converts the {@link Cursor} to a {@link List} of {@link TModel} and then closes it.
     */
    @Nullable
    public <TCustom extends BaseQueryModel> List<TCustom> toCustomListClose(Class<TCustom> customClass) {
        if (cursor != null) {
            return FlowManager.getQueryModelAdapter(customClass).getListModelLoader().load(cursor);
        } else {
            return null;
        }
    }

    /**
     * @return The first {@link TModel} of items from the contained {@link Cursor}. You must call {@link #close()} when finished.
     */
    @Nullable
    public TModel toModel() {
        if (cursor != null) {
            return retrievalAdapter.getSingleModelLoader().convertToData(cursor, null);
        } else {
            return null;
        }
    }

    /**
     * @return Converts the {@link Cursor} into the first {@link TModel} from the cursor and then closes it.
     */
    @Nullable
    public TModel toModelClose() {
        if (cursor != null) {
            return retrievalAdapter.getSingleModelLoader().load(cursor);
        } else {
            return null;
        }
    }

    public long count() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Nullable
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
    }
}
