package com.example.hellosensor;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private int azimuth;
    private Sensor accelerometer, magnetometer, rotationV;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    ImageView compass_image;
    TextView txt_azimuth;
    boolean haveSensor = false, haveSensor2 = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_image = (ImageView) findViewById(R.id.compass_image);
        txt_azimuth = (TextView) findViewById(R.id.azimuth);

        start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0])+ 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        azimuth = Math.round(azimuth);
        compass_image.setRotation(-azimuth);

        String where = "NW";

        if (azimuth >= 350 || azimuth <= 10)
            where = "N";
        if (azimuth < 350 && azimuth > 280)
            where = "NW";
        if (azimuth <= 280 && azimuth > 260)
            where = "W";
        if (azimuth <= 260 && azimuth > 190)
            where = "SW";
        if (azimuth <= 190 && azimuth > 170)
            where = "S";
        if (azimuth <= 170 && azimuth > 100)
            where = "SE";
        if (azimuth <= 100 && azimuth > 80)
            where = "E";
        if (azimuth <= 80 && azimuth > 10)
            where = "NE";


        txt_azimuth.setText(azimuth + "° " + where);
    }

    public void start() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            rotationV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = sensorManager.registerListener(this, rotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        if (haveSensor) {
            sensorManager.unregisterListener(this, rotationV);
        }
        else {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
