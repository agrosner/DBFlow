package com.grosner.dbflow.sql;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Constructs the beginning of a SQL DELETE query
 */
public class Delete implements Query {

    /**
     * The database manager this query corresponds to
     */
    private final FlowManager mManager;

    /**
     * Constructs this with the default {@link com.grosner.dbflow.config.FlowManager}
     */
    public Delete() {
        this(FlowManager.getInstance());
    }

    /**
     * Constructs this with a custom {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param flowManager
     */
    public Delete(FlowManager flowManager) {
        mManager = flowManager;
    }

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table        The table we want to run this query from
     * @param <ModelClass> The table class
     * @return
     */
    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<ModelClass>(mManager, this, table);
    }

    @Override
    public String getQuery() {
        return new QueryBuilder()
                .append("DELETE")
                .appendSpace().getQuery();
    }
}
