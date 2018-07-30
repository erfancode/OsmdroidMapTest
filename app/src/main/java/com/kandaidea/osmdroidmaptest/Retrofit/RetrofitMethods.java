package com.kandaidea.osmdroidmaptest.Retrofit;

import com.kandaidea.osmdroidmaptest.Models.SectorModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitMethods
{


    public void getSectors(final GetJsonresult getJsob)
    {
        API api = new APIClient().start();
        Call<List<SectorModel>> call = api.getSectors();
        call.enqueue(new Callback<List<SectorModel>>()
        {
            @Override
            public void onResponse(Call<List<SectorModel>> call, Response<List<SectorModel>> response)
            {
                getJsob.JsonResultSec(response);
            }

            @Override
            public void onFailure(Call<List<SectorModel>> call, Throwable t)
            {
                getJsob.JsonResultFail(call, t);
            }
        });
    }
}
