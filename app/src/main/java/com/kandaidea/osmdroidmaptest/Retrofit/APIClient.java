package com.kandaidea.osmdroidmaptest.Retrofit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kandaidea.osmdroidmaptest.Models.Example;
import com.kandaidea.osmdroidmaptest.Models.Office;
import com.kandaidea.osmdroidmaptest.Models.SectorModel;
import com.kandaidea.osmdroidmaptest.Realm.realmObjects.Sector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class APIClient
{

    public static final String TAG = APIClient.class.getSimpleName();
    private String BASE_URL = "http://10.0.2.2/SampleWS/Service1.asmx/";
    public API start()
    {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(okHttpClient)
                            .build();


        return retrofit.create(API.class);

    }

/*
    @Override
    public void onResponse(Call<List<SectorModel>> call, Response<List<SectorModel>> response)
    {
        if(response.isSuccessful())
        {
            Log.d(TAG, "getFromServer");
            List<SectorModel> sectorList = response.body();
            for (int i = 0 ; i < sectorList.size(); i++)
            {
                Log.i(TAG, String.valueOf(sectorList.get(i).getX()) + " , " + String.valueOf(sectorList.get(i).getY()));
            }
            Log.d(TAG, "getFromServer" + sectorList);
        }
        else
        {
            Log.d(TAG, "errorGetFromServer" + response.message());
            Log.d(TAG, "errorGetFromServer" + response.body());
        }
    }

    @Override
    public void onFailure(Call<List<SectorModel>> call, Throwable t)
    {
        Log.d(TAG, "onFailure");
        t.printStackTrace();
    }
    */
}
