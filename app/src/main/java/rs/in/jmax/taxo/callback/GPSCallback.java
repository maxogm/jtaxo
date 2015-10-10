package rs.in.jmax.taxo.callback;

import android.location.Location;

public interface GPSCallback {
    void onGPSUpdate(Location location);
    void onProviderDisabled();
    void onProviderEnabled();
}
