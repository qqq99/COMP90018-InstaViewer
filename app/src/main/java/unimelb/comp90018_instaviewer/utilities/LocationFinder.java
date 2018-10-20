package unimelb.comp90018_instaviewer.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import timber.log.Timber;

public class LocationFinder {
    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;
    private LocationManager locationManager;

//    public LocationFinder(Activity activity) {
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
//        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if (location != null) {
//                                setLocation(location);
//                            }
//                        }
//                    });
//        }
//    }

    public LocationFinder(Activity activity) {
        this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        Timber.d("Initialized location finder");

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Timber.d("Location finder current location: " + location);
                setLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                Timber.d("Location finder provider enabled: " + provider);
            }

            public void onProviderDisabled(String provider) {
                Timber.d("Location finder provider disabled: " + provider);
            }
        };

        Timber.d("Location finder permission enabled: " + ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION));

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            List<String> providers = locationManager.getProviders(true);

            Timber.d("location providers: " + providers.size());

            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                Timber.d("last known location, provider: %s, location: %s", provider,
                        l);
                Timber.d("last known location, location:" + l);

                if (l == null) {
                    continue;
                }
                if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                    Timber.d("found best last known location: %s", l);
                    bestLocation = l;
                }
            }
            if (bestLocation != null) {
                this.location = bestLocation;
                Timber.d("Best location: " + bestLocation);
            }
        }
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public double getLat() {
        if (location != null) {
            return location.getLatitude();
        }
        return 0;
    }

    public double getLon() {
        if (location != null) {
            return location.getLatitude();
        }
        return 0;
    }

}
