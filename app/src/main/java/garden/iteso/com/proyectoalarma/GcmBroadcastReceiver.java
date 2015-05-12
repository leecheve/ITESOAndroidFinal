package garden.iteso.com.proyectoalarma;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


/**
 * Created by luisneto on 5/10/2015.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {
        final Intent myIntent = intent;

        // Check if global alerts are disabled in Settings, if this is the case just return out
        //  of here
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean doGlobalAlerts = sharedPreferences.getBoolean("global_alerts_checkbox", false);

        if (!doGlobalAlerts) {
            Log.i("LUIS", "Global Alerts disabled. NO ALERT FOR YOU!");
            return;
        }

        // Get current location using GPS or Network, whatever comes first.
        // Handle only the first location provided and turn off location requests.
        locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(locationListener);

                String myLatitude = Double.toString(location.getLatitude());
                String myLongitude = Double.toString(location.getLongitude());

                String myLocation = "Lat: " + myLatitude + ". Lng: " + myLongitude;

                Log.i("LUIS", "Location: " + myLocation);

                // Check if the device is 50 meters around the sensor location, if it is
                //  don't do any alert.
                if (isInOffset(context, location, 50)) {
                    Log.i("LUIS", "user is in offset. NO ALERT FOR YOU!");
                    return;
                }

                Log.i("LUIS", "user is not in offset. Trigger Alert!");

                // Explicitly specify that GcmIntentService will handle the intent.
                ComponentName comp = new ComponentName(context.getPackageName(),
                        GcmIntentService.class.getName());
                // Start the service, keeping the device awake while it is launching.
                startWakefulService(context, (myIntent.setComponent(comp)));
                setResultCode(Activity.RESULT_OK);
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

        // Requests location updates from Network and GPS, whatever comes first.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, locationListener);
    }

    /*
      Check if the device is around the sensor offset meters. Used to prevent triggering an
      alarm when the used itself activated the sensor.
     */
    public boolean isInOffset(Context context, Location location, int offset) {
        //Position, decimal degrees
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        //Earth’s radius, sphere
        int R = 6378137;

        //Coordinate offsets in radians
        double dLat = (double)offset/ (double)R;
        double dLon = (double)offset/(R*Math.cos(Math.PI * lat / 180));

        //OffsetPosition, decimal degrees
        double latOffset = dLat * 180/Math.PI;
        double lonOffset = dLon * 180/Math.PI;

        double sensorLat = (double) sharedPreferences.getFloat("Latitud", 0f);
        double sensorLon = (double) sharedPreferences.getFloat("Longitud", 0f);

        /*
        // Nice debugging skills eh!

        Log.i("LUIS", "sensorLat: " + Double.toString(sensorLat) + ". sensorLon: " +
            Double.toString(sensorLon));
        Log.i("LUIS", "latOffset: " + Double.toString(latOffset) + ". lonOffset: " +
            Double.toString(lonOffset));
        Log.i("LUIS", "currLat: " + Double.toString(lat) + ". currLon: " +
            Double.toString(lon));
        */

        if ((lat + latOffset) > sensorLat && (lat - latOffset) < sensorLat)
            if ((lon + lonOffset > sensorLon) && (lon - lonOffset < sensorLon))
                return true;

        return false;
    }
}
