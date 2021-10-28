package com.example.compassapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView img_compass;
    TextView txt_azimuth;
    Button unlockButton;
    int mAzimuth;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    float[] rMat = new float[9];
    float[] orientation = new float[9];
    private float[] mLastAccerlerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean haveSensor = false, haveSensor2 = false;
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private LocationManager locationManager;
    public double longitude;
    public double lattitude;
    public fPoint currentPoint = new fPoint();
    public fPoint calgaryTower = new fPoint();

    public boolean password1 = false;
    public boolean password2 = false;
    public boolean password3 = false;
    public boolean password4 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calgaryTower.x = -114.0631021286443;
        calgaryTower.y = 51.04432145019242;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        img_compass = (ImageView) findViewById(R.id.img_compass);
        txt_azimuth = (TextView) findViewById(R.id.txt_azimuth);
        unlockButton = (Button) findViewById(R.id.unlockButton);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                lattitude = location.getLatitude();
                currentPoint.x = longitude;
                currentPoint.y = lattitude;
                Log.d("LOGGING", longitude+" "+lattitude);
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
        });

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rMat,event.values);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat,orientation)[0])+360)%360);
        }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values,0,mLastAccerlerometer,0,event.values.length);
            mLastAccelerometerSet = true;
        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values,0,mLastMagnetometer,0,event.values.length);
            mLastMagnetometerSet = true;
        }

        if(mLastMagnetometerSet && mLastAccelerometerSet){
            SensorManager.getRotationMatrix(rMat,null,mLastAccerlerometer,mLastMagnetometer);
            SensorManager.getOrientation(rMat,orientation);
            mAzimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMat,orientation)[0])+360)%360);
        }

        mAzimuth = Math.round(mAzimuth);
        img_compass.setRotation(-mAzimuth);

        String where = "NO";

        if(mAzimuth >= 350 || mAzimuth <= 10){
            where = "N";
            password1 = true;
            unlockButton.setBackgroundColor(0x00FF0000);
            Log.d("UNLOCKING", "Password 1 Unlocked");
        }
        if(mAzimuth < 350 && mAzimuth > 280){
            where = "NW";
        }
        if(mAzimuth <= 280 && mAzimuth > 260){
            where = "W";
        }
        if(mAzimuth <= 260 && mAzimuth > 190){
            where = "SW";
            if(password1 == true) {
                password2 = true;
                Log.d("UNLOCKING", "Password 2 Unlocked");
            }
        }
        if(mAzimuth <= 190 && mAzimuth > 170){
            where="S";
            if(password1 == true && password2 == true && password3 == true) {
                password4 = true;
                Log.d("UNLOCKING", "Password 4 Unlocked");
            }
        }
        if(mAzimuth <= 170 && mAzimuth > 100){
            where="SE";
        }
        if(mAzimuth <= 100 && mAzimuth > 80){
            where = "E";
            if(password2 == true) {
                password3 = true;
                Log.d("UNLOCKING", "Password 3 Unlocked");
            }
        }
        if(mAzimuth <=80 && mAzimuth > 10){
            where = "NE";
        }

        txt_azimuth.setText(mAzimuth + "° "+where);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void start(){
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null){
            if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null || mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null){
                noSensorAlert();
            }else{
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                haveSensor = mSensorManager.registerListener(this,mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this,mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this,mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your Device doesn't support the compass").setCancelable(false).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

    }

    public void stop(){
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }else{
            mSensorManager.unregisterListener(this,mRotationV);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        stop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        start();
    }

    double sign (fPoint p1, fPoint p2, fPoint p3)
    {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    public boolean PointInTriangle (fPoint pt, fPoint v1, fPoint v2, fPoint v3)
    {
        double d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(pt, v1, v2);
        d2 = sign(pt, v2, v3);
        d3 = sign(pt, v3, v1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);


        return !(has_neg && has_pos);
    }

    public fPoint getPointOnAngle(int compassAngle, fPoint p){

        double distance = 201;
        double angle;
        fPoint newP = new fPoint();
        while(compassAngle<0)
        {
            compassAngle = compassAngle+360;
        }
        while(compassAngle>360)
        {
            compassAngle = compassAngle-360;
        }
        if(compassAngle >= 0 && compassAngle <= 180){
            angle = 90-compassAngle;
            double radians = angle*Math.PI/180.0;
            newP.x = (Math.cos(radians) * distance) + p.x;
            newP.y = (Math.sin(radians) * distance) + p.y;
        }
        if(compassAngle > 180 && compassAngle <= 360){
            angle = compassAngle - 270;
            double radians = angle* Math.PI/180.0;
            newP.x = (Math.cos(radians) * (-distance)) + p.x;
            newP.y = (Math.sin(radians) * distance) + p.y;
        }

        return newP;
    }

    public void unlockPhone(View view){

        Log.d("LOGGING", "Running unlock function");
        password4 = false;
        if(password1 == true && password2 == true && password3 == true && password4 == true){
            Log.d("UNLOCK", "The phone is now unlocked!");

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }else{
            password1 = false;
            password2 = false;
            password3 = false;
            password4 = false;
            Log.d("LOCKING", "All Passwords Locked");
            unlockButton.setBackgroundColor(0x66FF0000);
        }


        fPoint a = getPointOnAngle(mAzimuth+20, currentPoint);
        fPoint b = getPointOnAngle(mAzimuth-20, currentPoint);


    }

}
