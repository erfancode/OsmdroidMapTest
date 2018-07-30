package com.kandaidea.osmdroidmaptest.map;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;


import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.kandaidea.osmdroidmaptest.Models.SectorModel;
import com.kandaidea.osmdroidmaptest.R;
import com.kandaidea.osmdroidmaptest.Realm.realmObjects.Sector;
import com.kandaidea.osmdroidmaptest.Retrofit.APIClient;
import com.kandaidea.osmdroidmaptest.Retrofit.GetJsonresult;
import com.kandaidea.osmdroidmaptest.Retrofit.RetrofitMethods;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.BuildConfig;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
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
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.util.PointReducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;


public class OsmMapView extends APIClient
{
    private static final String TAG = OsmMapView.class.getSimpleName();
    private MapView mMapView;
    private Context mContext;
    private Activity mActivity;
    private Polygon areaPolygon = new Polygon();
    private ArrayList<Marker> areaPolygonMarkers = new ArrayList<>();
    private Boolean firstLong = true;
    private Polygon tehranPolygon = new Polygon();
    private ArrayList<SectorModel> sectors ;
    private ArrayList<Polygon> sectorPolygons = new ArrayList<>();

    public OsmMapView(Context mContext, MapView mMapView, Activity mActivity)
    {
        this.mContext = mContext;
        this.mMapView = mMapView;
        this.mActivity = mActivity;
        //use for manage resources
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        areaPolygon.setFillColor(Color.RED);
        new RetrofitMethods().getSectors(new GetJsonresult()
        {
            @Override
            public void JsonResultSec(Response<List<SectorModel>> body)
            {
                if(body.isSuccessful())
                {
                    sectors = new ArrayList<>(body.body());
                    drawSectors();
                }
            }

            @Override
            public void JsonResultFail(Call<List<SectorModel>> call, Throwable t)
            {
                Log.e(TAG, "failedToGetSectorDataFromServer");
            }
        });
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
    public void setUpMap() throws ExecutionException, InterruptedException
    {

        /*
        MapEventsReceiver mer = new MapEventsReceiver()
        {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p)
            {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p)
            {
                if(mMapView.getOverlays().contains(areaPolygon))
                {
                    mMapView.getOverlays().remove(areaPolygon);
                }
                Marker newMarker = new Marker(mMapView);
                newMarker.setPosition(p);
                newMarker.setDraggable(true);
                newMarker.setIcon(mContext.getResources().getDrawable(R.drawable.ic_marker_edge_polygon));
                newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                newMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener()
                {
                    @Override
                    public void onMarkerDrag(Marker marker)
                    {
                        Log.d(TAG, "onMarkerDrag");
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker)
                    {
                        Log.d(TAG, "onMarkerDragEnd");

                        int index = areaPolygonMarkers.indexOf(marker);
                        GeoPoint p = marker.getPosition();
                        List<GeoPoint> x = areaPolygon.getPoints();
                        Log.d(TAG, "sizeOfPolygon is : " + x.size());
                        x.set(index, p);
                        Polygon poly = new Polygon();
                        poly.setPoints(x);
                        poly.setFillColor(Color.RED);
                        areaPolygon = poly;
                        mMapView.getOverlays().add(areaPolygon);
                        mMapView.getOverlays().remove(areaPolygonMarkers);
                        mMapView.getOverlays().addAll(areaPolygonMarkers);
                        mMapView.invalidate();
                    }

                    @Override
                    public void onMarkerDragStart(Marker marker)
                    {
                        Log.d(TAG, "onMarkerDragStart");
                        mMapView.getOverlays().remove(areaPolygon);
                        mMapView.invalidate();
                    }
                });
                mMapView.getOverlays().remove(areaPolygonMarkers);
                areaPolygonMarkers.add(newMarker);
                areaPolygon.addPoint(p);
                mMapView.getOverlays().add(areaPolygon);
                mMapView.getOverlays().addAll(areaPolygonMarkers);
                mMapView.invalidate();
                //Toast.makeText(mContext, "clicked : " + p.getLatitude() + " " + p.getLongitude()+ " and copied to clipboard",Toast.LENGTH_LONG).show();
                //ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                //ClipData clip = ClipData.newPlainText("", String.valueOf(p.getLatitude()) + "," + String.valueOf(p.getLongitude()));
                //clipboard.setPrimaryClip(clip);
                return false;
            }
        };

        MapEventsOverlay OverlayEventos = new MapEventsOverlay(mContext, mer);
        mMapView.getOverlays().add(OverlayEventos);
        */
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
                mMapView.invalidate();
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

        mMapView.setMinZoomLevel((double) 5);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);
        RotationGestureOverlay rgv = new RotationGestureOverlay(mContext, mMapView);
        rgv.setEnabled(true);
        mMapView.getOverlays().add(rgv);
        //add my location + compass
        //MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mContext), mMapView);
        //myLocationNewOverlay.enableMyLocation();
        //CompassOverlay mCompas = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext), mMapView);
        //mCompas.enableCompass();
        //mMapView.getOverlays().add(myLocationNewOverlay);


        /*
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
        */

        final IMapController mapController = mMapView.getController();
        mapController.setZoom(9);

        //makeRoad(new GeoPoint(35.71d, 51.37d), new GeoPoint(35.46d, 51d));
        //new GetData().execute();
        final Polygon circle = new Polygon(mMapView);
        circle.setPoints(Polygon.pointsAsCircle(new GeoPoint(35.764333d,50.914012d), 1000.0));
        circle.setStrokeColor(Color.BLACK);
        circle.setStrokeWidth(20);


        //new GetPOI().execute();
        //new GetKML().execute();
        makePointWithEdit();
        final Polygon[] polygons = new Polygon[3];
        final Polygon[] polygonss = new Polygon[3];


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

        /*
        mapController.setCenter(new GeoPoint(35.764333d,50.914012d));
        mapController.setZoom(15.6f);
        //new GetTile().execute();

        final SearchView searchView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                List<Address> addresses = searchForAddress(String.valueOf(searchView.getQuery()));
                Address res = addresses.get(0);

                if(res != null)
                {
                    ArrayList<GeoPoint> polygonPoints = res.getExtras().getParcelableArrayList("polygonpoints");
                    final ArrayList<Marker> markersPolygonPoints = new ArrayList<>();

                    if(polygonPoints.size() != 0 && polygonPoints != null)
                    {

                        tehranPolygon.setPoints(PointReducer.reduceWithTolerance(polygonPoints, .0001));
                        for(GeoPoint p : tehranPolygon.getPoints())
                        {
                            Marker newMarker = new Marker(mMapView);
                            newMarker.setPosition(p);
                            newMarker.setIcon(mContext.getResources().getDrawable(R.drawable.ic_marker_edge_polygon));
                            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            newMarker.setDraggable(true);
                            newMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener()
                            {
                                @Override
                                public void onMarkerDrag(Marker marker)
                                {

                                }

                                @Override
                                public void onMarkerDragEnd(Marker marker)
                                {
                                    int index = markersPolygonPoints.indexOf(marker);
                                    GeoPoint p = marker.getPosition();
                                    List<GeoPoint> x = tehranPolygon.getPoints();
                                    x.set(index, p);
                                    Polygon poly = new Polygon();
                                    poly.setPoints(x);
                                    poly.setStrokeColor(Color.BLUE);
                                    poly.setStrokeWidth(2);
                                    tehranPolygon = poly;
                                    mMapView.getOverlays().add(tehranPolygon);
                                    mMapView.getOverlays().remove(markersPolygonPoints);
                                    mMapView.getOverlays().addAll(markersPolygonPoints);
                                    mMapView.invalidate();
                                }

                                @Override
                                public void onMarkerDragStart(Marker marker)
                                {
                                    mMapView.getOverlays().remove(tehranPolygon);
                                    mMapView.invalidate();
                                }
                            });
                            markersPolygonPoints.add(newMarker);
                        }
                        tehranPolygon.setStrokeColor(Color.BLUE);
                        tehranPolygon.setStrokeWidth(2);
                        mMapView.getOverlays().add(tehranPolygon);
                        mMapView.getOverlays().addAll(markersPolygonPoints);
                        mMapView.invalidate();
                    }
                    mapController.setCenter(new GeoPoint(res.getLatitude(), res.getLongitude()));
                    mapController.setZoom(12.0f);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                return false;
            }
        });*/
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
                pois.add(new GeoPoint(35.692847 + i * .01, 51.129095 + i * .01));
            }
            RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(mContext);

            for (GeoPoint poi:pois){
                Marker poiMarker = new Marker(mMapView);
                poiMarker.setPosition(poi);
                poiMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener()
                {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView)
                    {
                        mMapView.getOverlays().remove(marker);
                        marker.setIcon(new ColorDrawable(0xffff5215));
                        mMapView.getOverlays().add(marker);
                        return false;
                    }
                });
                poiMarkers.add(poiMarker);
            }
            mMapView.getOverlays().add(poiMarkers);
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
    class GetLocation extends AsyncTask<String, Void, List<Address>>
    {
        @Override
        protected List<Address> doInBackground(String... voids)
        {
            GeocoderNominatim location = new GeocoderNominatim("tehran");
            location.setOptions(true);
            List<Address> result = null;
            try
            {
                result = location.getFromLocationName(voids[0], 10);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Log.d(TAG, "GetLocation " + result.get(0).getFeatureName());
            return result;
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
    private Polygon getSectorPolygon(GeoPoint base, double rad, float azimuth , float hbw, String name)
    {
        Polygon polygon = new Polygon(mMapView);
        polygon.addPoint(base);
        int times = (int) (hbw * 5);
        for(int i = 0; i <= times;i++)
        {
            polygon.addPoint(getEdge(base, (int) rad, (i * 0.2) + azimuth - (hbw / 2)));
        }
        return polygon;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void makePointWithEdit()
    {
        ArrayList<GeoPoint> pois = new ArrayList<>();
        for(int i = 0; i < 20; i++)
        {
            pois.add(new GeoPoint(35.692847 + i * .04, 51.129095 + i * .04));
        }
        RadiusMarkerClusterer poiMarkers = new RadiusMarkerClusterer(mContext);

        for (GeoPoint poi:pois){
            MarkerWithLabel poiMarker = new MarkerWithLabel(mMapView, "his name");
            poiMarker.setPosition(poi);
            poiMarker.setIcon(mActivity.getDrawable(R.mipmap.circule));
            poiMarkers.add(poiMarker);
        }
        mMapView.getOverlays().add(poiMarkers);
    }
    private List<Address> searchForAddress(String locationName)
    {
        AsyncTask<String, Void, List<Address>> result = new GetLocation().execute(locationName);
        try
        {
            return result.get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void drawSectors()
    {
        if(sectors.size() != 0 && sectors != null)
        {
            for(int i = 0; i < sectors.size(); i++)
            {
                SectorModel model = sectors.get(i);
                Polygon newPoly = getSectorPolygon(new GeoPoint(model.getX(),model.getY()), model.getR(), model.getAzimuth(), model.gethBW(), " " );
                sectorPolygons.add(newPoly);

            }
            if(mMapView.getOverlays().contains(sectorPolygons.get(0)))
            {
                //mMapView.getOverlays().remove(sectorPolygons);
            }
            else
            {
                mMapView.getOverlays().addAll(sectorPolygons);

            }
        }
    }
}
