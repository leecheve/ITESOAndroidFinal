package garden.iteso.com.proyectoalarma;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MapPane extends Activity implements OnMapReadyCallback {
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        final GoogleMap googleMap = map;

        SharedPreferences sharedPreference =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        float latitud = sharedPreference.getFloat("Latitud", 0f);
        float longitud = sharedPreference.getFloat("Longitud", 0f);

        if (latitud != 0f && longitud != 0f) {
            LatLng latLng = new LatLng(latitud, longitud);
            placeMarker(googleMap, latLng);
        }

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                placeMarker(googleMap, latLng);

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit()
                        .putFloat("Latitud", (float) latLng.latitude)
                        .putFloat("Longitud", (float) latLng.longitude)
                        .apply();

                setResult(RESULT_OK);
            }
        });

        map.setMyLocationEnabled(true);
    }

    private void placeMarker(GoogleMap googleMap, LatLng latLng) {
        if (marker == null) {
            // create marker
            marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Sensor Location")
                            .draggable(true)
            );

            // Move camera to marker
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 16));
        } else {
            marker.setPosition(latLng);
        }
    }
}