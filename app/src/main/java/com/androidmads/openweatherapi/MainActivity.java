package com.androidmads.openweatherapi;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ajts.androidmads.fontutils.FontUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;

import dalvik.system.PathClassLoader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;
import static java.lang.Math.round;

public class MainActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private static final int MY_REQUEST_INT = 177;
    public  static double laOwn = 0;
    public  static double laChoose = 0;
    public  static double loOwn = 0;
    public  static double loChoose = 0;

    public static String BaseUrl = "http://api.openweathermap.org/";
    public static String AppId = "2e65127e909e178d0af311a81f39948c";
    public static String lat = "35";
    public static String lon = "139";
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker currentUserLocationMarker;
    private TextView weatherData;
    private static final int Request_User_Location_Code = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            checkUserLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        weatherData = findViewById(R.id.textView);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Lato-Bold.ttf");
        FontUtils fontUtils = new FontUtils();
        fontUtils.applyFontToView(weatherData, typeface);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentData();
            }
        });
    }

    private void calcDistance() {
        Location loc1 = new Location("");
        loc1.setLatitude(laOwn);
        loc1.setLongitude(loOwn);

        Location loc2 = new Location("");
        loc2.setLatitude(laChoose);
        loc2.setLongitude(loChoose);

        float distanceInMeters = loc1.distanceTo(loc2);
        if (laOwn !=0 && loOwn != 0 && laChoose != 0 && loChoose !=0)
        {
            weatherData.setText("Distanz: " + distanceInMeters + "m");
            weatherData.setTextColor(Color.GRAY);
        }

    }

    void getCurrentData() {
        lat = String.valueOf(laChoose);
        lon = String.valueOf(loChoose);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData(lat, lon, AppId);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.code() == 200) {
                    WeatherResponse weatherResponse = response.body();
                    double temperature = weatherResponse.main.temp - 273.15;
                    double temperaturemin = weatherResponse.main.temp_min - 273.15;
                    double temperaturemax = weatherResponse.main.temp_max - 273.15;
                    assert weatherResponse != null;
                    String stringBuilder = "Country: " +
                            weatherResponse.sys.country +
                            "\n" +
                            "Temperature: " +
                            round(temperature)+"°" +
                            "\n" +
                            "Temperature(Min): " +
                            round(temperaturemin)+"°"+
                            "\n" +
                            "Temperature(Max): " +
                            round(temperaturemax)+"°" +
                            "\n" +
                            "Weather: " +
                            weatherResponse.weather.get(0).main +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main.humidity +
                            "\n" +
                            "Pressure: " +
                            weatherResponse.main.pressure;
                    if(weatherResponse.weather.get(0).main.equalsIgnoreCase("Rain"))
                    {
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(100);
                        weatherData.setText(stringBuilder + "\n" + "Infomation: Nimmm bitte Schirm mit");
                        weatherData.setTextColor(Color.RED);
                    }
                    else {
                        weatherData.setText(stringBuilder);
                        weatherData.setTextColor(Color.GRAY);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                weatherData.setText(t.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        weatherData.setText("Setze deine Location fest indem du auf deine aktuelle Position tippst");
        weatherData.setTextColor(Color.GRAY);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},MY_REQUEST_INT);
            }
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                laChoose = point.latitude;
                loChoose = point.longitude;

                Marker setMarkerOnMap =  mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(point.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED)));
                calcDistance();
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                if(laOwn != 0 && loOwn != 0)
                {
                    LatLng position2 = new LatLng(laOwn, loOwn);
                    Marker curretnPosition = mMap.addMarker(new MarkerOptions()
                            .position(position2)
                            .title(position2.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN)));
                    calcDistance();
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(position2));
                }


            }
        });
    }

    public boolean checkUserLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            return  false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient ==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Permission Denied",Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient(){

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (currentUserLocationMarker != null)
        {
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions= new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("USER CURRENT POSITION");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN));
        currentUserLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));
        if (googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener) this);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED )
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Deine Location wurde aktualisiert", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        LatLng position2 = new LatLng(location.getLatitude(), location.getLongitude());
        laOwn = location.getLatitude();
        loOwn = location.getLongitude();
        Marker curretnPosition = mMap.addMarker(new MarkerOptions()
                .position(position2)
                .title(position2.toString())
                .icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN)));
        calcDistance();
        weatherData.setText("Deine aktuelle Position wurde fixiert. Geben Sie ihr gewünschte Zielort ein");
        weatherData.setTextColor(Color.GRAY);
    }
}
