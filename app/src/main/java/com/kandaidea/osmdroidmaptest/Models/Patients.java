package com.kandaidea.osmdroidmaptest.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kandaidea.osmdroidmaptest.Realm.realmObjects.Sector;

import java.util.List;

public class Patients
{
    @SerializedName("patients")
    @Expose
    private List<SectorModel> models;

    public List<SectorModel> getModels()
    {
        return models;
    }

    public void setModels(List<SectorModel> models)
    {
        this.models = models;
    }
}
