package com.emspexample.gravimetro

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null

    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView

    private lateinit var locationManager: LocationManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvX = findViewById(R.id.tv_x)
        tvY = findViewById(R.id.tv_y)
        tvZ = findViewById(R.id.tv_z)
        tvLatitude = findViewById(R.id.tv_latitude)
        tvLongitude = findViewById(R.id.tv_longitude)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if(gravitySensor==null){
            tvX.text = "Sensor de gravedad no disponible"
        }

        val locationProvider = LocationManager.GPS_PROVIDER
        val isGPSEnabled = locationManager.isProviderEnabled(locationProvider)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGPSEnabled) {
            tvLatitude.text = "GPS no disponible"
            tvLongitude.text = "GPS no disponible"
        }else{
            if (!isNetworkEnabled) {
                tvLatitude.text = "Network no disponible"
                tvLongitude.text = "Network no disponible"
            }
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)

        
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null) {
            tvLatitude.text = "Ultima latitud conocida: ${lastKnownLocation.latitude}"
            tvLongitude.text = "Ultima longitud conocida: ${lastKnownLocation.longitude}"
        }else{
            tvLatitude.text = "Esperando ubicación..."
            tvLongitude.text = "Esperando ubicación..."
        }

    }
        override fun onResume() {
            super.onResume()
            gravitySensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        override fun onPause() {
            super.onPause()
            sensorManager.unregisterListener(this)
            locationManager.removeUpdates(this)

        }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            tvX.text = "X: $x"
            tvY.text = "Y: $y"
            tvZ.text = "Z: $z"
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy

        if (latitude == 0.0 && longitude == 0.0) {
            tvLatitude.text = "Ubicación no disponible"
            tvLongitude.text = "Ubicación no disponible"
        } else {
            tvLatitude.text = "Latitude: $latitude (Precisión: $accuracy)"
            tvLongitude.text = "Longitude: $longitude (Precisión: $accuracy)"
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
                } catch (e: SecurityException) {
                    Log.e("LocationError", "Error al solicitar ubicación: ${e.message}")
                }
            } else {

                tvLatitude.text = "Permiso de ubicación no otorgado"
                tvLongitude.text = "Permiso de ubicación no otorgado"
                Toast.makeText(this, "Permiso de ubicación no otorgado", Toast.LENGTH_SHORT).show()
            }
        }
    }



}