package com.grosner.dbflow.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Will notifies all {@link com.grosner.dbflow.runtime.observer.ModelObserver} that the model has been
 * saved, updated, or inserted.
 */
@Ignore
public abstract class BaseNotifiableModel extends BaseModel {

    @Override
    public void save(boolean async) {
        SqlUtils.save(FlowManager.getInstance(), this, async, SqlUtils.SAVE_MODE_DEFAULT, true);
    }

    public void insert(boolean async) {
        SqlUtils.save(FlowManager.getInstance(), this, async, SqlUtils.SAVE_MODE_INSERT, true);
    }

    @Override
    public void update(boolean async) {
        SqlUtils.save(FlowManager.getInstance(), this, async, SqlUtils.SAVE_MODE_UPDATE, true);
    }

    @Override
    public void delete(boolean async) {
        SqlUtils.delete(FlowManager.getInstance(), this, async, true);
    }
}
