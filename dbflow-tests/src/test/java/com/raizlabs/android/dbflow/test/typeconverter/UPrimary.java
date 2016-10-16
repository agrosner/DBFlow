package com.raizlabs.android.dbflow.test.typeconverter;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.UUID;

/**
 * Description: Example of a class with type converted primary key used as a {@link ForeignKey}.
 *
 * @author Andrew Grosner (fuzz)
 */
@Table(database = TestDatabase.class)
public class UPrimary {

    @PrimaryKey
    UUID uuid;

}
