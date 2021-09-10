package com.raizlabs.android.dbflow.structure;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The base implementation of {@link Model}. It is recommended to use this class as
 * the base for your {@link Model}, but it is not required.
 */
@SuppressWarnings("unchecked")
public class BaseModel implements Model {

    /**
     * Specifies the Action that was taken when data changes
     */
    public enum Action {

        /**
         * The model called {@link Model#save()}
         */
        SAVE,

        /**
         * The model called {@link Model#insert()}
         */
        INSERT,

        /**
         * The model called {@link Model#update()}
         */
        UPDATE,

        /**
         * The model called {@link Model#delete()}
         */
        DELETE,

        /**
         * The model was changed. used in prior to {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}
         */
        CHANGE
    }

    @ColumnIgnore
    private transient ModelAdapter modelAdapter;

    @Override
    public void load() {
        getModelAdapter().load(this);
    }

    @Override
    public void load(@NonNull DatabaseWrapper wrapper) {
        getModelAdapter().load(this, wrapper);
    }

    @Override
    public boolean save() {
        return getModelAdapter().save(this);
    }


    @Override
    public boolean save(@NonNull DatabaseWrapper databaseWrapper) {
        return getModelAdapter().save(this, databaseWrapper);
    }

    @Override
    public boolean delete() {
        return getModelAdapter().delete(this);
    }

    @Override
    public boolean delete(@NonNull DatabaseWrapper databaseWrapper) {
        return getModelAdapter().delete(this, databaseWrapper);
    }

    @Override
    public boolean update() {
        return getModelAdapter().update(this);
    }

    @Override
    public boolean update(@NonNull DatabaseWrapper databaseWrapper) {
        return getModelAdapter().update(this, databaseWrapper);
    }

    @Override
    public long insert() {
        return getModelAdapter().insert(this);
    }

    @Override
    public long insert(DatabaseWrapper databaseWrapper) {
        return getModelAdapter().insert(this, databaseWrapper);
    }

    @Override
    public boolean exists() {
        return getModelAdapter().exists(this);
    }

    @Override
    public boolean exists(@NonNull DatabaseWrapper databaseWrapper) {
        return getModelAdapter().exists(this, databaseWrapper);
    }

    @NonNull
    @Override
    public AsyncModel<? extends Model> async() {
        return new AsyncModel<>(this);
    }

    /**
     * @return The associated {@link ModelAdapter}. The {@link FlowManager}
     * may throw a {@link InvalidDBConfiguration} for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    public ModelAdapter getModelAdapter() {
        if (modelAdapter == null) {
            modelAdapter = FlowManager.getModelAdapter(getClass());
        }
        return modelAdapter;
    }
}
