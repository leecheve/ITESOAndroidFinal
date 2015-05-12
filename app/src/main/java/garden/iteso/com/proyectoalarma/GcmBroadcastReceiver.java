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

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luisneto on 5/10/2015.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onReceive(final Context context, Intent intent) {
        final Context myContext = context;
        final Intent myIntent = intent;

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

                if (isInOffset(context, location, 50)) {
                    Log.i("LUIS", "user is in offset. NO ALARM FOR YOU!");
                    return;
                }

                Log.i("LUIS", "user is not in offset. Trigger Alarm!");

                // Explicitly specify that GcmIntentService will handle the intent.
                ComponentName comp = new ComponentName(myContext.getPackageName(),
                        GcmIntentService.class.getName());
                // Start the service, keeping the device awake while it is launching.
                startWakefulService(myContext, (myIntent.setComponent(comp)));
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

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, locationListener);
        //myListener.onLocationChanged(new Location("gps"));


    }

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

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        double sensorLat = (double) sharedPreferences.getFloat("Latitud", 0f);
        double sensorLon = (double) sharedPreferences.getFloat("Longitud", 0f);

        Log.i("LUIS", "sensorLat: " + Double.toString(sensorLat) + ". sensorLon: " +
            Double.toString(sensorLon));
        Log.i("LUIS", "latOffset: " + Double.toString(latOffset) + ". lonOffset: " +
            Double.toString(lonOffset));
        Log.i("LUIS", "currLat: " + Double.toString(lat) + ". currLon: " +
            Double.toString(lon));

        if ((lat + latOffset) > sensorLat && (lat - latOffset) < sensorLat)
            if ((lon + lonOffset > sensorLon) && (lon - lonOffset < sensorLon))
                return true;

        return false;
    }
}
