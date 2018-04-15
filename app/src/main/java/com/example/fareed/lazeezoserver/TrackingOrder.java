package com.example.fareed.lazeezoserver;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telecom.ConnectionRequest;
import android.util.Log;
import android.widget.Toast;

import com.example.fareed.lazeezoserver.Common.Common;
import com.example.fareed.lazeezoserver.Common.DirectionJSONParser;
import com.example.fareed.lazeezoserver.Remote.IGeoCoordinates;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Downloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST=1000;
    private final static int LOCATION_PERMISSION_REQUEST=1001;

    private Location mLastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private static int UPDATE_INTERVAL=1000;
    private static int FASTEST_INTERVAL=5000;
    private static int DISPLACEMENT=10;

    private IGeoCoordinates mService;


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
         startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(googleApiClient!=null){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mService= Common.getGeoCodeService();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestRuntimePermission();
        }else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        displayLocation();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestRuntimePermission();
        }
        else{
            mLastLocation=LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (mLastLocation!=null){
                double latitude=mLastLocation.getLatitude();
                double longitude=mLastLocation.getLongitude();

                Log.i("MyLocation",latitude+","+longitude);
                LatLng yourLocation=new LatLng(latitude,longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));



                 drawRounte(yourLocation,Common.currentRequest.getAddress());

            }else {
               // Log.i("displayLocation: ","Mylocation");
               Toast.makeText(this, "Can't Get Location", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void drawRounte(final LatLng yourLocation, String address) {
        mService.getGeoCode(address).enqueue(new Callback<String>(){
           @Override
            public void onResponse(Call<String> call, Response<String> response){
               try{
                   JSONObject jsonObject =new JSONObject(response.body().toString());

                   String lat=((JSONArray)jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lat").toString();
                   String lng=((JSONArray)jsonObject.get("results"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lng").toString();
                   LatLng orderLocation =new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                   Drawable drawable=getResources().getDrawable(R.drawable.ic_shopping_cart_black_24dp);
                   //Bitmap bitmap=drawableToBitmap(drawable);
                   Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.ship);
                   bitmap=Common.scaleBitmap(bitmap,70,70);

                   MarkerOptions marker=new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                           .title("Order of "+Common.currentRequest.getPhone())
                           .position(orderLocation);
                   mMap.addMarker(marker);

                   mService.getDirections(yourLocation.latitude+","+yourLocation.longitude,
                           orderLocation.latitude+","+orderLocation.longitude)
                           .enqueue(new Callback<String>() {
                               @Override
                               public void onResponse(Call<String> call, Response<String> response) {
                                   Log.i("onRespone", response.body().toString());
                                   new ParserTask().execute(response.body().toString());
                               }

                               @Override
                               public void onFailure(Call<String> call, Throwable t) {

                               }
                           });

               }catch (JSONException e){
                   e.printStackTrace();
               }
           }

           @Override
            public void onFailure(Call<String> call,Throwable t){

           }
        });
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    private void createLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        googleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else {
                Toast.makeText(this, "The device is not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();

                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog pd=new ProgressDialog(TrackingOrder.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Please wait");
            pd.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes=null;
            try {
                jObject=new JSONObject(strings[0]);
                DirectionJSONParser parser=new DirectionJSONParser();

                routes=parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
            pd.dismiss();

//            ArrayList points = new ArrayList();;
//            PolylineOptions lineOptions = new PolylineOptions();;
//            lineOptions.width(2);
//            lineOptions.color(Color.RED);
//            MarkerOptions markerOptions = new MarkerOptions();
//            // Traversing through all the routes
//            for(int i=0;i<lists.size();i++){
//                // Fetching i-th route
//                List<HashMap<String, String>> path = lists.get(i);
//                // Fetching all the points in i-th route
//                for(int j=0;j<path.size();j++){
//                    HashMap<String,String> point = path.get(j);
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//                    points.add(position);
//                }
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(points);
//                lineOptions.width(12);
//                lineOptions.color(Color.BLUE);
//                lineOptions.geodesic(true);
//            }


            ArrayList points=null;
            PolylineOptions lineOptions=null;
            Log.i("listSize=",String.valueOf(lists.size()));
            for(int i=0; i<lists.size(); i++){
                points=new ArrayList();
                lineOptions=new PolylineOptions();
                List<HashMap<String, String>> path=lists.get(i);

                for (int j=0; j<path.size(); j++){
                    HashMap<String, String> point=path.get(j);

                    double lat=Double.parseDouble(point.get("lat"));
                    double lng=Double.parseDouble(point.get("lng"));

                    LatLng position=new LatLng(lat,lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            if(points.size()!=0){
                Toast.makeText(TrackingOrder.this, "In if", Toast.LENGTH_SHORT).show();
                mMap.addPolyline(lineOptions);//to avoid crash
            }
           // mMap.addPolyline(lineOptions);
        }
    }
}
