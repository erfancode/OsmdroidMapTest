package com.kandaidea.osmdroidmaptest.Realm.realmObjects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sector extends RealmObject
{
    @PrimaryKey
    private String name;
    private double latitude;
    private double longitude;
    private int radius;
    private int hbw;
    private int azimuth;

    //region Getter and Setter
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    public int getHbw()
    {
        return hbw;
    }

    public void setHbw(int hbw)
    {
        this.hbw = hbw;
    }

    public int getAzimuth()
    {
        return azimuth;
    }

    public void setAzimuth(int azimuth)
    {
        this.azimuth = azimuth;
    }
    //endregion


}
