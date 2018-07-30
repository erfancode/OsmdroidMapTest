package com.kandaidea.osmdroidmaptest.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SectorModel
{

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("X")
    @Expose
    private double x;
    @SerializedName("Y")
    @Expose
    private double y;
    @SerializedName("R")
    @Expose
    private double r;
    @SerializedName("HBW")
    @Expose
    private float hBW;
    @SerializedName("azimuth")
    @Expose
    private float azimuth;

    public SectorModel(Long id, double x, double y, double r, float hBW, float azimuth)
    {
        this.id = id;
        this.x = x;
        this.y = y;
        this.r = r;
        this.hBW = hBW;
        this.azimuth = azimuth;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getR()
    {
        return r;
    }

    public void setR(double r)
    {
        this.r = r;
    }

    public float gethBW()
    {
        return hBW;
    }

    public void sethBW(float hBW)
    {
        this.hBW = hBW;
    }

    public float getAzimuth()
    {
        return azimuth;
    }

    public void setAzimuth(float azimuth)
    {
        this.azimuth = azimuth;
    }
}
