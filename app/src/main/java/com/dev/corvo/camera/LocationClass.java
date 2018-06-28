package com.dev.corvo.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

/** Open Camera is written by Mark Harman.
 *
 * Handles listening for GPS location (both coarse and fine).
 *
 * this class is taken from Open Camera project (Open Source)
 *
 * All credits goes to him for providing this class
 *
 * link : https://opencamera.sourceforge.io/
 */
public class LocationClass {
    private final Context context;
    public boolean store_location = false;
    private final LocationManager locationManager;
    private MyLocationListener [] locationListeners;

    LocationClass(Context context) {
        this.context = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public android.location.Location getLocation() {
        if( locationListeners == null )
            return null;
        for(MyLocationListener locationListener : locationListeners) {
            android.location.Location location = locationListener.getLocation();
            if( location != null )
                return location;
        }
        return null;
    }

    private static class MyLocationListener implements LocationListener {
        private android.location.Location location;
        volatile boolean test_has_received_location;

        android.location.Location getLocation() {
            return location;
        }

        public void onLocationChanged(android.location.Location location) {

            if( location != null && ( location.getLatitude() != 0.0d || location.getLongitude() != 0.0d ) ) {
                this.location = location;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch( status ) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                {
                    this.location = null;
                    this.test_has_received_location = false;
                    break;
                }
                default:
                    break;
            }
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            this.location = null;
            this.test_has_received_location = false;
        }
    }

    @SuppressLint("MissingPermission")
    void setupLocationListener() {
        if( store_location && locationListeners == null ) {


            locationListeners = new MyLocationListener[2];
            locationListeners[0] = new MyLocationListener();
            locationListeners[1] = new MyLocationListener();

            if( locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) ) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListeners[1]);
            }
            if( locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListeners[0]);
            }
        }
        else if( !store_location ) {
            freeLocationListeners();
        }
    }

    void freeLocationListeners() {
        if( locationListeners != null ) {
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                boolean has_coarse_location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean has_fine_location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if( !has_coarse_location_permission && !has_fine_location_permission ) {
                    return;
                }
            }
            for(int i=0;i<locationListeners.length;i++) {
                locationManager.removeUpdates(locationListeners[i]);
                locationListeners[i] = null;
            }
            locationListeners = null;
        }
    }


    @Deprecated
    public static String locationToDMS(double coord) {
        String sign = (coord < 0.0) ? "-" : "";
        coord = Math.abs(coord);
        int intPart = (int)coord;
        boolean is_zero = (intPart==0);
        String degrees = String.valueOf(intPart);
        double mod = coord - intPart;

        coord = mod * 60;
        intPart = (int)coord;
        is_zero = is_zero && (intPart==0);
        mod = coord - intPart;
        String minutes = String.valueOf(intPart);

        coord = mod * 60;
        intPart = (int)coord;
        is_zero = is_zero && (intPart==0);
        String seconds = String.valueOf(intPart);

        if( is_zero ) {
            sign = "";
        }

        return sign + degrees + "\u00b0" + minutes + "'" + seconds + "\"";
    }
}
