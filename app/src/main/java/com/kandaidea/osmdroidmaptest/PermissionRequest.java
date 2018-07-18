package com.kandaidea.osmdroidmaptest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionRequest
{
    private Context context;
    private Activity activity;
    public PermissionRequest(Context context, Activity activity)
    {
        this.context = context;
        this.activity = activity;
    }
    public void request(String[] permissions)
    {
        for(String s: permissions)
        {
            if(ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(activity, new String[]{s}, 99);
            }
        }
    }
}
