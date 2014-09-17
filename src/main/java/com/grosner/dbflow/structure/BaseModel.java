package com.grosner.dbflow.structure;

import android.database.Cursor;

import com.grosner.dbflow.sql.SqlUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Ignore
public class BaseModel implements Model {

    @Override
    public void save(boolean async) {
        SqlUtils.save(this, async, SqlUtils.SAVE_MODE_DEFAULT);
    }

    @Override
    public void delete(boolean async) {
        SqlUtils.delete(this, async);
    }

    @Override
    public void update(boolean async) {
        SqlUtils.save(this, async, SqlUtils.SAVE_MODE_UPDATE);
    }

    @Override
    public void load(Cursor cursor) {
        SqlUtils.loadFromCursor(this, cursor);
    }
}
