package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorIterator;
import com.raizlabs.android.dbflow.list.IFlowCursorIterator;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: A class that contains a {@link Cursor} and handy methods for retrieving data from it.
 * You must close this object post use via {@link #close()}.
 */
public class CursorResult<TModel> implements IFlowCursorIterator<TModel> {

    private final InstanceAdapter<TModel> retrievalAdapter;

    @Nullable
    private FlowCursor cursor;

    @SuppressWarnings("unchecked")
    CursorResult(Class<TModel> modelClass, @Nullable Cursor cursor) {
        if (cursor != null) {
            this.cursor = FlowCursor.from(cursor);
        }
        retrievalAdapter = FlowManager.getInstanceAdapter(modelClass);
    }

    /**
     * Swaps the current cursor and will close existing one.
     */
    public void swapCursor(@Nullable FlowCursor cursor) {
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
    @NonNull
    public List<TModel> toList() {
        return cursor != null
            ? retrievalAdapter.getListModelLoader().convertToData(cursor, null)
            : new ArrayList<TModel>();
    }

    /**
     * @return Converts the {@link Cursor} to a {@link List} of {@link TModel} and then closes it.
     */
    @NonNull
    public List<TModel> toListClose() {
        final List<TModel> list = cursor != null
            ? retrievalAdapter.getListModelLoader().load(cursor)
            : new ArrayList<TModel>();
        close();
        return list;
    }

    /**
     * @return A {@link List} of items from this object. You must call {@link #close()} when finished.
     */
    @NonNull
    public <TCustom> List<TCustom> toCustomList(@NonNull Class<TCustom> customClass) {
        return cursor != null ? FlowManager.getQueryModelAdapter(customClass)
            .getListModelLoader().convertToData(cursor, null) : new ArrayList<TCustom>();
    }

    /**
     * @return Converts the {@link Cursor} to a {@link List} of {@link TModel} and then closes it.
     */
    @NonNull
    public <TCustom> List<TCustom> toCustomListClose(@NonNull Class<TCustom> customClass) {
        final List<TCustom> customList = cursor != null ? FlowManager.getQueryModelAdapter(customClass)
            .getListModelLoader().load(cursor) : new ArrayList<TCustom>();
        close();
        return customList;
    }

    /**
     * @return The first {@link TModel} of items from the contained {@link Cursor}. You must call {@link #close()} when finished.
     */
    @Nullable
    public TModel toModel() {
        return cursor != null ? retrievalAdapter.getSingleModelLoader().convertToData(cursor, null) : null;
    }

    /**
     * @return Converts the {@link Cursor} into the first {@link TModel} from the cursor and then closes it.
     */
    @Nullable
    public TModel toModelClose() {
        final TModel model = cursor != null ? retrievalAdapter.getSingleModelLoader().load(cursor) : null;
        close();
        return model;
    }

    /**
     * @return The first {@link TModel} of items from the contained {@link Cursor}. You must call {@link #close()} when finished.
     */
    @Nullable
    public <TCustom> TCustom toCustomModel(@NonNull Class<TCustom> customClass) {
        return cursor != null ? FlowManager.getQueryModelAdapter(customClass)
            .getSingleModelLoader().convertToData(cursor, null) : null;
    }

    /**
     * @return Converts the {@link Cursor} to a {@link TModel} and then closes it.
     */
    @Nullable
    public <TCustom> TCustom toCustomModelClose(@NonNull Class<TCustom> customClass) {
        final TCustom customList = cursor != null ? FlowManager.getQueryModelAdapter(customClass)
            .getSingleModelLoader().load(cursor) : null;
        close();
        return customList;
    }

    @Nullable
    @Override
    public TModel getItem(long position) {
        TModel model = null;
        if (cursor != null && cursor.moveToPosition((int) position)) {
            model = retrievalAdapter.getSingleModelLoader().convertToData(cursor, null, false);
        }
        return model;
    }

    @NonNull
    @Override
    public FlowCursorIterator<TModel> iterator() {
        return new FlowCursorIterator<>(this);
    }

    @NonNull
    @Override
    public FlowCursorIterator<TModel> iterator(int startingLocation, long limit) {
        return new FlowCursorIterator<>(this, startingLocation, limit);
    }

    @Override
    public long getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    @Nullable
    public Cursor cursor() {
        return cursor;
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
    }
}
