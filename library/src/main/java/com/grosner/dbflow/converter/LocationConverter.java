package com.grosner.dbflow.converter;

import android.location.Location;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines how we store and retrieve a {@link android.location.Location}
 */
public class LocationConverter implements TypeConverter<String, Location> {

    @Override
    public String getDBValue(Location model) {
        return String.valueOf(model.getLatitude()) + "," + model.getLongitude();
    }

    @Override
    public Location getModelValue(String data) {
        String[] values = data.split(",");
        if (values.length < 2) {
            return null;
        } else {
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(values[0]));
            location.setLongitude(Double.parseDouble(values[1]));
            return location;
        }
    }
}
