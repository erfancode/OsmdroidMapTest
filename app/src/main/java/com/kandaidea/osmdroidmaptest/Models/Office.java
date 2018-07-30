package com.kandaidea.osmdroidmaptest.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.osmdroid.views.overlay.Polygon;

import java.util.List;

public class Office
{
    @SerializedName("patients")
    @Expose
    private List<SectorModel> patients = null;

    public List<SectorModel> getPatients() {
        return patients;
    }

    public void setPatients(List<SectorModel> patients) {
        this.patients = patients;
    }
}
