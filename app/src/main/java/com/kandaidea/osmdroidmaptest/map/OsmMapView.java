package com.kandaidea.osmdroidmaptest.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;


import com.kandaidea.osmdroidmaptest.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.BuildConfig;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.util.PointReducer;

import java.util.ArrayList;


public class OsmMapView
{
    private static final String TAG = OsmMapView.class.getSimpleName();
    private MapView mMapView;
    private Context mContext;
    private Activity mActivity;

    public OsmMapView(Context mContext, MapView mMapView, Activity mActivity)
    {
        this.mContext = mContext;
        this.mMapView = mMapView;
        this.mActivity = mActivity;
        //use for manage resources
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    protected static float cleanValue(float p_val, float p_limit)
    {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }

    public static void adjustHue(ColorMatrix cm, float value)
    {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0)
        {
            return;
        }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]
                {
                        lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                        0f, 0f, 0f, 1f, 0f
                };
        cm.postConcat(new ColorMatrix(mat));
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setUpMap()
    {

        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);


        final MapTileProviderBasic anotherTileProvider = new MapTileProviderBasic(mContext);
        final ITileSource anotherTileSource = new XYTileSource("MyCustomTiles", 4, 12, 256, ".png?type=google",
                new String[]{"http://wms.chartbundle.com/tms/v1.0/enrl/"});
        anotherTileProvider.setTileSource(anotherTileSource);
        final TilesOverlay secondTilesOverlay = new TilesOverlay(anotherTileProvider, mContext);
        secondTilesOverlay.setLoadingBackgroundColor(mContext.getResources().getColor(R.color.xxx));
        final ColorMatrix negate =new ColorMatrix();
        negate.set(new float[]{
                0.5f,0,0,0,0,        //red
                0,0.5f,0,0,0,//green
                0,0,.5f,0,0,//blue
                0,0,0,.5f,0 //alpha
        });
        adjustHue(negate, 255);
        secondTilesOverlay.setColorFilter(new ColorMatrixColorFilter(negate));
        mMapView.getOverlays().add(secondTilesOverlay);

        SeekBar seekBar = mActivity.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                float bri = (float)(seekBar.getProgress()) / 100.0f;
                Log.d(TAG, "seekbar is : " + bri);
                mMapView.getOverlays().remove(secondTilesOverlay);
                negate.set(new float[]{
                        bri,0,0,0,0,        //red
                        0,bri,0,0,0,//green
                        0,0,bri,0,0,//blue
                        0,0,0,bri,0 //alpha
                });
                adjustHue(negate, 255);
                secondTilesOverlay.setColorFilter(new ColorMatrixColorFilter(negate));
                mMapView.getOverlays().add(secondTilesOverlay);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

       // mMapView.setTileSource(TileSourceFactory.USGS_SAT);
        mMapView.setMinZoomLevel((double) 5);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        RotationGestureOverlay rgv = new RotationGestureOverlay(mContext, mMapView);
        rgv.setEnabled(true);
        mMapView.getOverlays().add(rgv);
        //add my location + compass
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMapView);
        myLocationNewOverlay.enableMyLocation();
        /*CompassOverlay mCompas = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext), mMapView);
        mCompas.enableCompass();
        */
        mMapView.getOverlays().add(myLocationNewOverlay);

        //enable rotation gesture




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
        //new GetData().execute();
        final Polygon circle = new Polygon(mMapView);
        circle.setPoints(Polygon.pointsAsCircle(new GeoPoint(35.764333d,50.914012d), 1000.0));

        circle.setStrokeColor(Color.BLACK);
        circle.setStrokeWidth(20);


        new GetPOI().execute();
        //new GetKML().execute();
        final Polygon[] polygons = new Polygon[3];
        final Polygon[] polygonss = new Polygon[3];

        final Marker centerPolygons = new Marker(mMapView);
        centerPolygons.setPosition(new GeoPoint(35.764333d,50.914012d));
        centerPolygons.setIcon(mContext.getResources().getDrawable(R.mipmap.ic_radio));
        for(int i = 0; i < 3; i++)
        {
            Polygon polygon1 = getSectorPolygon(new GeoPoint(35.764333d,50.914012d),1000,i * 120, 60,"ALB0011-MU-A");
            polygon1.setFillColor(Color.YELLOW);
            polygon1.setStrokeColor(Color.BLACK);
            polygon1.setStrokeWidth(5);
            polygons[i] = polygon1;
            polygon1.setOnClickListener(new Polygon.OnClickListener()
            {
                @Override
                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos)
                {
                    Toast.makeText(mContext, "ALB0011-MU-A", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        mMapView.setMapListener(new MapListener()
        {
            @Override
            public boolean onScroll(ScrollEvent event)
            {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event)
            {
                double x = mMapView.getZoomLevelDouble();
                Log.d(TAG, "onZoom" + x + "  " + mMapView.getOverlays().contains(circle));
                if(x < 14.0d)
                {
                    Log.d(TAG, "onZoomRemove");
                    if(mMapView.getOverlays().contains(polygons[0]))
                    {
                        mMapView.getOverlays().remove(polygons[2]);
                        mMapView.getOverlays().remove(polygons[1]);
                        mMapView.getOverlays().remove(polygons[0]);
                        //mMapView.getOverlays().add(centerPolygons);

                        Polygon m1 = new Polygon();
                        Polygon m2 = new Polygon();
                        Polygon m3 = new Polygon();
                        m1.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[0].getPoints()), (1 /(2 * x))*0.0001d));
                        polygonss[0] = m1;
                        m2.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[1].getPoints()), (1 /(2 * x))*0.0001d));
                        polygonss[1] = m2;
                        m3.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[2].getPoints()), (1 /(2 * x))*0.0001d));
                        polygonss[2] = m3;
                        Log.d(TAG,  "polygons[0] size is : " + polygonss[0].getPoints().size() + " "+ polygonss[1].getPoints().size() + " " + polygonss[2].getPoints().size());

                       // polygonss[0].setPoints(m);
                       // polygonss[1].setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[1].getPoints()), .5f));
                       // polygonss[2].setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[2].getPoints()), .5f));

                        mMapView.getOverlays().add(polygonss[0]);
                        mMapView.getOverlays().add(polygonss[1]);
                        mMapView.getOverlays().add(polygonss[2]);
                        Log.d(TAG, "reduce pointsSize is : " + polygonss[0].getPoints().size() );
                    }
                    else if(mMapView.getOverlays().contains(polygonss[0]))
                    {
                        mMapView.getOverlays().remove(polygonss[0]);
                        mMapView.getOverlays().remove(polygonss[1]);
                        mMapView.getOverlays().remove(polygonss[2]);

                        Polygon m1 = new Polygon();
                        Polygon m2 = new Polygon();
                        Polygon m3 = new Polygon();
                        m1.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[0].getPoints()), (5000 /(Math.pow(x,4)))*0.001d));
                        m1.setStrokeWidth(5);

                        polygonss[0] = m1;
                        m2.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[1].getPoints()), (5000 /(Math.pow(x,4)))*0.001d));
                        m2.setStrokeWidth(5);
                        polygonss[1] = m2;
                        m3.setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[2].getPoints()), (5000 /(Math.pow(x,4)))*0.001d));
                        m3.setStrokeWidth(5);
                        polygonss[2] = m3;
                        Log.d(TAG,  "polygons[0] size is : " + polygonss[0].getPoints().size() + " "+ polygonss[1].getPoints().size() + " " + polygonss[2].getPoints().size());

                        // polygonss[0].setPoints(m);
                        // polygonss[1].setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[1].getPoints()), .5f));
                        // polygonss[2].setPoints(PointReducer.reduceWithTolerance(new ArrayList<GeoPoint>(polygons[2].getPoints()), .5f));

                        mMapView.getOverlays().add(polygonss[0]);
                        mMapView.getOverlays().add(polygonss[1]);
                        mMapView.getOverlays().add(polygonss[2]);
                        Log.d(TAG, "reduce pointsSize is : " + polygonss[0].getPoints().size() );
                    }
                }
                else
                {
                    if(!mMapView.getOverlays().contains(polygons[0]))
                    {
                        Log.d(TAG, "onZoomAdd");
                        mMapView.getOverlays().add(polygons[0]);
                        mMapView.getOverlays().add(polygons[1]);
                        mMapView.getOverlays().add(polygons[2]);
                        Log.d(TAG, "pointsSize is : " + polygons[0].getPoints().size());
                        mMapView.getOverlays().remove(polygonss[0]);
                        mMapView.getOverlays().remove(polygonss[1]);
                        mMapView.getOverlays().remove(polygonss[2]);
                    }
                }
                return false;
            }
        });


        mapController.setCenter(new GeoPoint(35.764333d,50.914012d));
        mapController.setZoom(15.6f);
        //new GetTile().execute();



    }

    public void makeRoad(GeoPoint start, GeoPoint end)
    {
        RoadManager roadManager = new OSRMRoadManager(mContext);
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(start);
        waypoints.add(end);
        //waypoints.add(new GeoPoint(35.692507, 51.487972));
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
            GeoPoint end = new GeoPoint(35.692507, 51.487972);//new GeoPoint(35.46d, 51d);
            makeRoad(start, end);
            return null;
        }
    }
    class GetPOI extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            GeoPoint startPoint = new GeoPoint(35.71d, 51.37d);
            ArrayList<GeoPoint> pois = new ArrayList<>();
            for(int i = 0; i < 100; i++)
            {
                pois.add(new GeoPoint(37 + i * .01, 50 + i * .01));
            }
            RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(mContext);
            mMapView.getOverlays().add(poiMarkers);

            for (GeoPoint poi:pois){
                Marker poiMarker = new Marker(mMapView);
                poiMarker.setPosition(poi);
                poiMarkers.add(poiMarker);
            }
            return null;
        }
    }
    class GetKML extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseKMLUrl("http://mapsengine.google.com/map/kml?forcekml=1&mid=z6IJfj90QEd4.kUUY9FoHFRdE");
            FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(mMapView, null, null, kmlDocument);
            mMapView.getOverlays().add(kmlOverlay);

            return null;
        }
    }
    class GetTile extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            String[] urlArray = {"http://tile.openstreetmap.org/"};
            mMapView.setTileSource(new XYTileSource("online", 0, 18, 256, ".png", urlArray  ));

            return null;
        }
    }

    private GeoPoint getEdge(GeoPoint base, int rad, double azimuth )
    {
        double R = 6378.1 ;
        double brng = Math.toRadians(azimuth);
        double d = rad / 1000 ;

        double lat1 = Math.toRadians(base.getLatitude());
        double lon1 = Math.toRadians(base.getLongitude());
        double lat2 = Math.asin( (Math.sin(lat1)*Math.cos(d/R)) +
                (Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng)));

        double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                Math.cos(d/R)-(Math.sin(lat1)*Math.sin(lat2)));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        GeoPoint point1 = new GeoPoint(lat2, lon2);
        return point1;
    }
    private Polygon getSectorPolygon(GeoPoint base, int rad, int azimuth , int hbw, String name)
    {
        Polygon polygon = new Polygon(mMapView);
        polygon.addPoint(base);
        int times = hbw * 5;
        for(int i = 0; i <= times;i++)
        {
            polygon.addPoint(getEdge(base, rad, (i * 0.2) + azimuth - (hbw / 2)));
        }
        return polygon;
    }
}
