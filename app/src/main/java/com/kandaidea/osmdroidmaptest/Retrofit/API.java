package com.kandaidea.osmdroidmaptest.Retrofit;

import com.kandaidea.osmdroidmaptest.Models.Example;
import com.kandaidea.osmdroidmaptest.Models.Office;
import com.kandaidea.osmdroidmaptest.Models.SectorModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface API
{
    @GET("GetSector")
    Call<List<SectorModel>> getSectors();
}
