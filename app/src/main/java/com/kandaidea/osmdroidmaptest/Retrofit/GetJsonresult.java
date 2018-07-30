package com.kandaidea.osmdroidmaptest.Retrofit;

import com.kandaidea.osmdroidmaptest.Models.SectorModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public interface GetJsonresult
{
    void JsonResultSec(Response<List<SectorModel>> body);
    void JsonResultFail(Call<List<SectorModel>> call, Throwable t);
}
