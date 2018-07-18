package com.kandaidea.osmdroidmaptest.map;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class OsmMapView
{
    private static final String TAG = OsmMapView.class.getSimpleName();
    private MapView mMapView;
    private Context mContext;


    public OsmMapView(Context mContext, MapView mMapView)
    {
        this.mContext = mContext;
        this.mMapView = mMapView;
    }

    public void setUpMap()
    {
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setMinZoomLevel((double) 5);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        //add my location + compass
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMapView);
        myLocationNewOverlay.enableMyLocation();
        CompassOverlay mCompas = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext), mMapView);
        mCompas.enableCompass();

        mMapView.getOverlays().add(myLocationNewOverlay);
        mMapView.getOverlays().add(mCompas);

        //enable rotation gesture
        RotationGestureOverlay rgv = new RotationGestureOverlay(mContext, mMapView);
        rgv.setEnabled(true);
        mMapView.getOverlays().add(rgv);



        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title1", "Description1", new GeoPoint(0.0d,0.0d)));
        items.add(new OverlayItem("Title2", "Description2", new GeoPoint(50.0d,30.0d)));

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(mContext, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
                {

                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item)
                    {
                        Log.d(TAG, "onClicked item :" + index);
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item)
                    {
                        Log.d(TAG, "onLongClicked item :" + index);
                        return false;
                    }
                });
        mOverlay.setFocusItemsOnTap(true);
        mMapView.getOverlays().add(mOverlay);


        GeoPoint startPoint = new GeoPoint(48.13, -1.63);
        IMapController mapController = mMapView.getController();
        mapController.setZoom(9);
        mapController.setCenter(startPoint);

        //makeRoad(new GeoPoint(35.71d, 51.37d), new GeoPoint(35.46d, 51d));
        new GetData().execute();
        Polygon circle = new Polygon(mMapView);
        circle.setPoints(Polygon.pointsAsCircle(new GeoPoint(35.71d, 51.37d), 20000.0));
        circle.setFillColor(0x12121212);
        circle.setStrokeColor(Color.RED);
        circle.setStrokeWidth(2);
        mMapView.getOverlays().add(circle);

    }

    public void makeRoad(GeoPoint start, GeoPoint end)
    {
        RoadManager roadManager = new OSRMRoadManager(mContext);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(start);
        waypoints.add(end);
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.setColor(Color.RED);
        roadOverlay.setWidth(5);
        mMapView.getOverlays().add(roadOverlay);
        //mMapView.invalidate();
    }
    class GetData extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            GeoPoint start = new GeoPoint(35.71d, 51.37d);
            GeoPoint end = new GeoPoint(35.46d, 51d);
            makeRoad(start, end);
            return null;
        }
    }
}
