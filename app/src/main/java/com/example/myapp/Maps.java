package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PLAY_SERVICES_ERROR_CODE = 9001;
    private FusedLocationProviderClient fusedLocationClient;
    private Polyline currentRoute;

    // EMSI locations in Morocco
    private static final LatLng[] EMSI_LOCATIONS = {
            new LatLng(33.589319, -7.605327),  // Casablanca
            new LatLng(34.020882, -6.841650),  // Rabat
            new LatLng(31.629472, -8.008955)   // Marrakech
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play Services not available");
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_maps);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment == null) {
            Log.e(TAG, "Map fragment not found");
            Toast.makeText(this, "Error initializing map", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkLocationPermission();
        mapFragment.getMapAsync(this);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_ERROR_CODE).show();
            } else {
                Log.e(TAG, "Device doesn't support Google Play Services");
            }
            return false;
        }
        return true;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    enableMyLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 12));
                            }
                        }
                    });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }

            // Add all EMSI locations
            for (LatLng location : EMSI_LOCATIONS) {
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("EMSI Center"));
            }

            // Set up marker click listener
            mMap.setOnMarkerClickListener(this);

            // Center map on Casablanca EMSI by default
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(EMSI_LOCATIONS[0], 10));

            // UI settings
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage());
            Toast.makeText(this, "Error setting up map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Check if we have location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        // Get last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                            LatLng destination = marker.getPosition();

                            // Draw route between current location and marker
                            drawRoute(current, destination);
                        }
                    }
                });

        return false;
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        // Clear previous route if exists
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // For demo purposes, we'll draw a straight line
        // In a real app, you would use Directions API to get the actual route
        currentRoute = mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(5)
                .color(Color.BLUE));

        // Zoom to show both points
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                new LatLngBounds .Builder()
                        .include(origin)
                        .include(destination)
                        .build(), 100));
    }
}