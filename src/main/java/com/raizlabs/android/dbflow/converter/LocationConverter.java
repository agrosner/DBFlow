package com.raizlabs.android.dbflow.converter;

import android.location.Location;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class LocationConverter implements TypeConverter<String,Location> {
    @Override
    public Class<String> getDatabaseType() {
        return String.class;
    }

    @Override
    public Class<Location> getModelType() {
        return Location.class;
    }

    @Override
    public String getDBValue(Location model) {
        return String.valueOf(model.getLatitude()) + "," + model.getLongitude();
    }

    @Override
    public Location getModelValue(String data) {
        String[] values = data.split(",");
        if(values.length < 2) {
            return null;
        } else {
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(values[0]));
            location.setLongitude(Double.parseDouble(values[1]));
            return location;
        }
    }
}
