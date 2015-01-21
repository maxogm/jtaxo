package rs.in.jmax.taxo.callback;

import android.location.Location;

public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
    public abstract void onProviderDisabled();
    public abstract void onProviderEnabled();
}
