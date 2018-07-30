package com.kandaidea.osmdroidmaptest.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Example
{


    @SerializedName("patients")
    private SectorModel[] models;

    public Example(SectorModel[] models)
    {
        this.models = models;
    }

    public SectorModel[] getModels()
    {
        return models;
    }

    public void setModels(SectorModel[] models)
    {
        this.models = models;
    }
}
