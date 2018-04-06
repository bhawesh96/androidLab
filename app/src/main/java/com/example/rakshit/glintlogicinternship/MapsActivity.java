package com.example.rakshit.glintlogicinternship;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import android.os.Vibrator;

import static java.lang.Double.NaN;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener
{
    private GoogleMap mMap;
    private Marker marker = null;
    private Polyline polyline = null;
    private List<LatLng> points = new ArrayList<>();

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    SensorManager sensorManager;
    Sensor accelSensor, gyroSensor, gravitySensor;
    TextView tx, ty, tz, tgx, tgy, tgz;

    Location location;
    double latitude;
    double longitude;

    LatLng prevLatLng = null;
    Vibrator vibrator;

    TextView tv_lat, tv_lon;

    protected LocationManager locationManager;

    // The minimum distance to change Updates in meters
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 seconds

    FirebaseAuth auth;
    DatabaseReference reference;
    String uid = "";
    LatLng agentLocation = null;
    ValueEventListener updateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tv_lat = (TextView)findViewById(R.id.textViewLatitude);
        tv_lon = (TextView)findViewById(R.id.textViewLongitude);
        tx = (TextView) findViewById(R.id.textViewgX);
        ty = (TextView) findViewById(R.id.textViewgY);
        tz = (TextView) findViewById(R.id.textViewgZ);
        tgx = (TextView)findViewById(R.id.textViewgravityX);
        tgy = (TextView)findViewById(R.id.textViewgravityY);
        tgz = (TextView)findViewById(R.id.textViewgravityZ);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        if (!Utils.isAdmin())
        {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        if (!Utils.isAdmin())
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!Utils.isAdmin())
        {
            Intent i = getIntent();
            uid = i.getStringExtra("uid");

            if (true || uid!=null && !uid.isEmpty())
            {
                updateListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        agentLocation = new LatLng((double)dataSnapshot.child("latitude").getValue(),
                                (double)dataSnapshot.child("longitude").getValue());
                        updateLocation(agentLocation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.e("Maps", databaseError.getMessage());
                    }
                };

                reference.child(auth.getCurrentUser().getUid()).child("co-ords").addValueEventListener(updateListener);
            }
        }
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gravityListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // GYROSCOPE
    SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tgx.setText("gyro X : " + (int)x);
            tgy.setText("gyro Y : " + (int)y);
            tgz.setText("gyro Z : " + (int)z);

            if(Math.abs(x) >= 8 || Math.abs(y) >= 8)
                vibrator.vibrate(500);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { }
    };

    // ACCELEROMETER
    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

//            tx.setText("accel X : " + (int)x);
//            ty.setText("accel Y : " + (int)y);
//            tz.setText("accel Z : " + (int)z);
        }
    };

    // GRAVITY
    SensorEventListener gravityListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tx.setText("gravity X : " + (int)x);
            ty.setText("gravity Y : " + (int)y);
            tz.setText("gravity Z : " + (int)z);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && requestCode==101)
        {
            getLocation();
            try
            {
                mMap.setMyLocationEnabled(true);
            }
            catch (SecurityException e)
            {
                Log.e("Maps", Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        try
        {
            if (!Utils.isAdmin())
            {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    getLocation();
                    mMap.setMyLocationEnabled(true);
                }
                else
                {
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                        }
                    }.run();
                }
            }
        }
        catch (SecurityException e)
        {
            Log.e("Maps", Log.getStackTraceString(e));
        }

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    public Location getLocation()
    {
        try
        {
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled)
            {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            }
            else
            {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            tv_lat.setText("Lat : " + Double.toString(location.getLatitude()));
                            tv_lon.setText("Lon : " + Double.toString(location.getLongitude()));
                            updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                tv_lat.setText("Lat : " + Double.toString(location.getLatitude()));
                                tv_lon.setText("Lon : " + Double.toString(location.getLongitude()));
                                updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                            }
                        }
                    }
                }
            }

        } catch (SecurityException e)
        {
            Log.e("Maps", Log.getStackTraceString(e));
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        //HERE RAHUL
        double x = 0.0;
        if(prevLatLng!=null)
            x = getDistance(prevLatLng, new LatLng(location.getLatitude(), location.getLongitude()));
        if(x == NaN){
            x = 0.0;
        }

        tv_lat.setText("dist : " + Double.toString(x));
        tv_lon.setText("Lon : " + Double.toString(location.getLongitude()));
        updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    private void updateLocation(final LatLng location)
    {
        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                int i;
                for(i=0 ;i<points.size();i++);
                if(i!=0)
                    prevLatLng = points.get(i-1);
                points.add(location);
                if (MapsActivity.this.marker != null)
                {
                    MapsActivity.this.marker.setPosition(location);
                    prevLatLng = location;
                }
                else
                {
                    MapsActivity.this.marker = mMap.addMarker(new MarkerOptions().position(location));
                }
                if (MapsActivity.this.polyline != null) {
                    MapsActivity.this.polyline.setPoints(points);

                }
                else
                    MapsActivity.this.polyline = mMap.addPolyline(new PolylineOptions().color(Color.BLUE).addAll(points));

                CameraUpdate cameraUpdate;
                if (points.size()<=1)
                     cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 12);
                else
                    cameraUpdate = CameraUpdateFactory.newLatLng(location);
                mMap.animateCamera(cameraUpdate);
            }
        });

        if (!Utils.isAdmin())
        {
            reference.child(auth.getCurrentUser().getUid()).child("co-ords").setValue(location).addOnCompleteListener(
                    new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (!task.isSuccessful())
                                Toast.makeText(MapsActivity.this, "Location update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            else {

                                Toast.makeText(MapsActivity.this, "Location value updated: ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (Utils.isAdmin())
            getMenuInflater().inflate(R.menu.menu_maps_admin, menu);
        else
            getMenuInflater().inflate(R.menu.menu_maps_agent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId()==R.id.menu_logout)
        {
            if (!Utils.isAdmin())
                locationManager.removeUpdates(this);
            else
                reference.removeEventListener(updateListener);
            auth.signOut();
            finish();
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        if (item.getItemId()==R.id.menu_test)
        {
            runDummyTest(uid);
        }
        if (item.getItemId()==android.R.id.home)
        {
            if (!Utils.isAdmin())
                locationManager.removeUpdates(this);
            else
                reference.removeEventListener(updateListener);
            finish();
        }
        return true;
    }

    private void runDummyTest(final String uid)
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                LatLng l = null;
                for (int i=0;i<40;i++)
                {
                    if (i<10)
                        l = new LatLng(agentLocation.latitude + 0.01, agentLocation.longitude + 0.01);
                    else if (i>=10 && i<20)
                        l = new LatLng(agentLocation.latitude - 0.01, agentLocation.longitude + 0.01);
                    else if (i>=20 && i<30)
                        l = new LatLng(agentLocation.latitude - 0.01, agentLocation.longitude - 0.01);
                    else
                        l = new LatLng(agentLocation.latitude + 0.01, agentLocation.longitude - 0.01);

                    reference.child(uid).child("co-ords").setValue(l).addOnCompleteListener(
                            new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (!task.isSuccessful())
                                        Toast.makeText(MapsActivity.this, "Test failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    try
                    {
                        Thread.sleep(200);
                    } catch (InterruptedException e)
                    {
                        Log.e("Maps", Log.getStackTraceString(e));
                    }
                }
                return null;
            }
        }.execute();
    }

    public double getDistance(LatLng prevLocation, LatLng currLocation) {
        return Math.sqrt((Math.pow(prevLocation.latitude - currLocation.latitude, 2) - Math.pow(prevLocation.longitude - currLocation.longitude, 2)));

    }
}
