package com.raizlabs.android.dbflow.converter;

import java.util.UUID;

/**
 * Description: Responsible for converting a {@link UUID} to a {@link String}.
 *
 * @author Andrew Grosner (fuzz)
 */
public class UUIDConverter extends TypeConverter<String, UUID> {

    @Override
    public String getDBValue(UUID model) {
        return model != null ? model.toString() : null;
    }

    @Override
    public UUID getModelValue(String data) {
        if (data == null) {
            return null;
        }
        return UUID.fromString(data);
    }
}
