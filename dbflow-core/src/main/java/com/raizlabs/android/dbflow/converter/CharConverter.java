package com.raizlabs.android.dbflow.converter;

/**
 * Description: Converts a {@link Character} into a {@link String} for database storage.
 */
public class CharConverter extends TypeConverter<String, Character> {

    @Override
    public String getDBValue(Character model) {
        return model != null ? new String(new char[]{model}) : null;
    }

    @Override
    public Character getModelValue(String data) {
        return data != null ? data.charAt(0) : null;
    }
}
