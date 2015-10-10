package rs.in.jmax.taxo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import rs.in.jmax.taxo.callback.GPSCallback;
import rs.in.jmax.taxo.manager.GPSManager;

public class JTaxo extends Activity implements GPSCallback {

    private final static double[] multipliers = {
            0.001, 1.0
    };

    private final static String[] unitstrings = {
            "km", "m"
    };

    private GPSManager gpsManager = null;
    private int unitindex = 0;
    private double currentLon = 0;
    private double currentLat = 0;
    private double startLon = 0;
    private double startLat = 0;
    private double distance = 0.0;
    private boolean isMeasuring = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Spinner spinner = (Spinner) findViewById(R.id.unitspinner);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onMeasurementUnitClicked);

        gpsManager = new GPSManager();

        gpsManager.startListening(this);
        gpsManager.setGPSCallback(this);

        // getting GPS status
        boolean isGPSEnabled = gpsManager.getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            showSettings();
        } else {
            ((Button) findViewById(R.id.startmeasuring)).setOnClickListener(onButtonClicked);
            ((Button) findViewById(R.id.stopmeasuring)).setOnClickListener(onButtonClicked);

            disableButtons();
            ((TextView) findViewById(R.id.info)).setText("GPS searching...");
        }

    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettings() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onProviderDisabled() {
        isMeasuring = false;
        startLon = 0.0;
        startLat = 0.0;
        distance = 0.0;
        disableButtons();
        ((TextView) findViewById(R.id.info)).setText("GPS is disabled...");
    }

    @Override
    public void onProviderEnabled() {
        ((TextView) findViewById(R.id.info)).setText("GPS searching...");
        disableButtons();
    }

    @Override
    public void onGPSUpdate(Location location) {
        currentLon = location.getLongitude();
        currentLat = location.getLatitude();

        if (location.hasAccuracy()) {
            ((TextView) findViewById(R.id.accuracy)).setText(location.getAccuracy() + "m");
            if (!isMeasuring) {
                enableButtons(false);
            }
        }

        if (location.hasSpeed()) {
            float speed = location.getSpeed();
            BigDecimal speedKmh = new BigDecimal(speed * 3.6d).setScale(1, RoundingMode.HALF_UP);
            ((TextView) findViewById(R.id.speed)).setText(speedKmh.toString() + "km/h");
        }

        if (isMeasuring) {
            updateMeasurement();
        } else {
            ((TextView) findViewById(R.id.info)).setText(getString(R.string.start_measuring_info));
        }
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
        super.onDestroy();
    }

    private void startMeasuring() {
        isMeasuring = true;
        startLon = currentLon;
        startLat = currentLat;
        ((TextView) findViewById(R.id.info)).setText(getString(R.string.measuring_info));
        enableButtons(true);
    }

    private void stopMeasuring() {
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm")
                .setMessage("Stop?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isMeasuring = false;
                        startLon = 0.0;
                        startLat = 0.0;
                        distance = 0.0;
                        ((TextView) findViewById(R.id.info)).setText(getString(R.string.start_measuring_info));
                        ((TextView) findViewById(R.id.distance)).setText("");
                        enableButtons(false);
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateMeasurement() {
        double calDistance = calcGeoDistance(startLat, startLon, currentLat, currentLon) * multipliers[unitindex];
        distance = RoundDecimal(distance + calDistance, 2);
        String distanceText = "" + distance + " " + unitstrings[unitindex];
        ((TextView) findViewById(R.id.distance)).setText(distanceText);
        startLon = currentLon;
        startLat = currentLat;
    }

    private void enableButtons(boolean isMeasuring) {
        ((Button) findViewById(R.id.startmeasuring)).setEnabled(isMeasuring ? false : true);
        ((Button) findViewById(R.id.stopmeasuring)).setEnabled(isMeasuring ? true : false);
        Spinner spinner = (Spinner) findViewById(R.id.unitspinner);
        spinner.setEnabled(!isMeasuring);
    }

    private void disableButtons() {
        ((Button) findViewById(R.id.startmeasuring)).setEnabled(false);
        ((Button) findViewById(R.id.stopmeasuring)).setEnabled(false);
        Spinner spinner = (Spinner) findViewById(R.id.unitspinner);
        spinner.setEnabled(false);
    }

    private double calcGeoDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        double distance = 0.0;

        try {
            final float[] results = new float[3];

            Location.distanceBetween(lat1, lon1, lat2, lon2, results);

            distance = results[0];
        } catch (final Exception ex) {
            distance = 0.0;
        }

        return distance;
    }

    public double RoundDecimal(double value, int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, 6);

        return bd.doubleValue();
    }

    private AdapterView.OnItemSelectedListener onMeasurementUnitClicked = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i("Item", "" + position + ", " + id);

            unitindex = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.startmeasuring: {
                    startMeasuring();
                    break;
                }
                case R.id.stopmeasuring: {
                    stopMeasuring();
                    break;
                }
            }
        }
    };
}
