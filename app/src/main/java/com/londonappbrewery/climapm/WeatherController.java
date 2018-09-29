package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    // Constants:
    //Location permission REQUEST CODE.
    final int REQUEST_CODE = 10;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "fb846298483da53470059ca74fbaf1b3";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 1000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    //Setting up locatin provider
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    //Setting up location services.
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeCityIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(changeCityIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();//Check the app lifecyle for this method
        Log.d("Weahter", "OnResume() called");

        Intent mIntent = getIntent();
        String city = mIntent.getStringExtra("City");

        if(city != null){
            getWeatherForNewCity(city);

        }
        else{
            Log.d("Weather", "Getting weather for current location");
            getWeatherForCurrentLocation();

        }



    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city){
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        getDataFromNetWork(params);

    }



    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("Weather", "onLocation() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                getDataFromNetWork(params);
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
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Weather","onRequestPermissionResult(): Permission Granted!" );

            }else{
                Log.d("Weather", "Permisssion denied =x");
            }

            /* NOTE
                In older verions of Android, the permission is granted during installation.
                In newer versions the permissions should be granted only when the apps needs to take action.


             */

        }
    }



    private void getDataFromNetWork(RequestParams p){

        AsyncHttpClient client = new AsyncHttpClient();
        //Runs the task in the background, while the app waits for the response

        client.get(WEATHER_URL, p, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Weather", "Success! Json" + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);


                updateUI(weatherData);




            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e("Weather", "fail" +  e.toString());
                Log.d("Weather", "Status Code" + statusCode);
                Toast.makeText(WeatherController.this,"request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(WeatherDataModel w){

        if(w == null){

            mTemperatureLabel.setText("Error");
            mCityLabel.setText("Error");

            return;
        }

        mTemperatureLabel.setText(w.getTemperature());
        mCityLabel.setText(w.getCity());

        int resourceID = getResources().getIdentifier(w.getMlconName(),"drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);



    }



    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();

        if(mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }
}
