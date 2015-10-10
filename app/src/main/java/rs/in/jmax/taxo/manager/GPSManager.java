package rs.in.jmax.taxo.manager;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import rs.in.jmax.taxo.callback.GPSCallback;

public class GPSManager {
    private static final int gpsMinTime = 2000;
    private static final int gpsMinDistance = 1;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private GPSCallback gpsCallback = null;

    public GPSManager() {
        locationListener = new LocationListener() {

            public void onProviderDisabled(String s) {
                gpsCallback.onProviderDisabled();
            }

            public void onProviderEnabled(String s) {
                gpsCallback.onProviderEnabled();
            }

            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }

            public void onLocationChanged(final Location location) {
                if (location != null && gpsCallback != null) {
                    gpsCallback.onGPSUpdate(location);
                }
            }
        };
    }

    public void startListening(final Activity activity) {
        if (locationManager == null) {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        }

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(true);

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_FINE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSManager.gpsMinTime, GPSManager.gpsMinDistance, locationListener);
    }

    public void stopListening() {
        try {
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            locationManager = null;
        } catch (final Exception ex) {

        }
    }

    public void setGPSCallback(final GPSCallback gpsCallback) {
        this.gpsCallback = gpsCallback;
    }

    public GPSCallback getGPSCallback() {
        return gpsCallback;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}