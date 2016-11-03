package com.quemb.qmbform.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quemb.qmbform.R;
import com.quemb.qmbform.services.FetchAddressIntentService;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.android.gms.common.ConnectionResult.SERVICE_DISABLED;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;

public class LocationPickerActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final Logger LOGGER = Logger.getLogger("LocationPickerActivity");

    private static final String MARKER_POSITION_SAVED_STATE = "marker_position_saved_state";
    private static final String MARKER_TITLE_SAVED_STATE = "marker_title_saved_state";
    private static final String PERMISSIONS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSIONS_COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String PERMISSIONS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    public static final String LOCATION_RESULT = "location_result";

    private static final int REQUEST_LOCATION_ACCESS = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private Object mLock;
    private Location mLastLocation;
    private LatLng mRestoredMarkerLatLng;
    private LocationRequest mLocationRequest;

    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private boolean mLocationServicesEnabled = false;
    private boolean mResultMightBePending = false;
    private boolean mLocationUpdateRequested = false;

    private String mMarkerAddress;
    private String mLocationAddress;
    private String mRestoredMarkerTitle;
    private Marker mMarker;
    private ProgressBar mProgressBar;
    private Handler mHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_picker);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Button doneButton = (Button) findViewById(R.id.doneButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mHandle = new Handler();
        mLock = new Object();

        setProgress(false);

        mLastLocation = new Location("");

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgress(true);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mLock) {
                                    if (!mResultMightBePending && mLastLocation != null) {
                                        HashMap<String, Object> resultSet = new HashMap<>();
                                        resultSet.put("longitude", mLastLocation.getLongitude());
                                        resultSet.put("latitude", mLastLocation.getLatitude());
                                        if (mMarker != null) {
                                            resultSet.put("_string", mMarkerAddress);
                                        } else {
                                            resultSet.put("_string", mLocationAddress);
                                        }

                                        finishWithResult(Activity.RESULT_OK, resultSet);
                                        setProgress(false);
                                        cancel();
                                    } else if (!mLocationServicesEnabled && !mResultMightBePending) {
                                        finishWithResult(Activity.RESULT_OK, new HashMap());
                                        setProgress(false);
                                        cancel();
                                    }
                                }
                            }
                        });
                    }
                }, 0, 100);
            }
        });

        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRestoredMarkerLatLng = savedInstanceState.getParcelable(MARKER_POSITION_SAVED_STATE);
        mRestoredMarkerTitle = savedInstanceState.getString(MARKER_TITLE_SAVED_STATE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMarker != null) {
            outState.putParcelable(MARKER_POSITION_SAVED_STATE, mMarker.getPosition());
            outState.putString(MARKER_TITLE_SAVED_STATE, mMarker.getTitle());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERMISSIONS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(PERMISSIONS_COURSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(PERMISSIONS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{ PERMISSIONS_FINE_LOCATION, PERMISSIONS_COURSE_LOCATION,
                        PERMISSIONS_NETWORK_STATE}, REQUEST_LOCATION_ACCESS);
            } else {
                mGoogleApiClient.connect();
            }
        } else {
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationSettings();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        String errorMessage = "";

        switch (connectionResult.getErrorCode()) {
            case SERVICE_VERSION_UPDATE_REQUIRED:
                errorMessage = "Google Play Services need to be updated before using the location prompt.";
                break;
            case SERVICE_MISSING:
                errorMessage = "Google Play Services are missing. Download Google Play Services in order to use this prompt.";
                break;
            case SERVICE_DISABLED:
                errorMessage = "Google Play Services seem to be disabled. Please enable Google Pay Services to use this prompt.";
                break;
            default:
                errorMessage = "Google Play Services unavailable.";
                break;
        }

        alertBuilder.setTitle("Google Play Services");
        alertBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishWithResult(RESULT_CANCELED, new HashMap());
            }
        });

        alertBuilder.setMessage(errorMessage);
        alertBuilder.setCancelable(false);
        alertBuilder.create().show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (mLocationUpdateRequested) {
            mLastLocation = location;
            startIntentService(FetchAddressIntentService.RequestType.LOCATION_REQUEST);
            mLocationUpdateRequested = false;
        }

        if (mLastLocation == null) {
            mLastLocation = location;

            setCamera(mLastLocation, true, true);
            startIntentService(FetchAddressIntentService.RequestType.LOCATION_REQUEST);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(mLocationServicesEnabled);

        if (mLocationServicesEnabled) {
            startLocationServices();
        }

        if (mRestoredMarkerLatLng != null && mRestoredMarkerTitle != null) {
            placeMarkerTo(mRestoredMarkerLatLng, mRestoredMarkerTitle);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (mLastLocation != null) {
                    LOGGER.log(Level.INFO, "Map was Clicked!");
                    placeMarkerTo(latLng);
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LOGGER.log(Level.INFO, "Location was Clicked!");
                mLocationUpdateRequested = true;
                removeMarker();
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        boolean success = true;

        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS: {

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        LOGGER.log(Level.FINEST, "Granted");

                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        LOGGER.log(Level.SEVERE, "Permission Denied");
                        success = false;
                    }
                }

                if (success) {
                    mGoogleApiClient.connect();
                } else {
                    finishWithResult(Activity.RESULT_CANCELED, new HashMap());
                }

                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void initiateMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment =
                (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        getFragmentManager().beginTransaction().add(autocompleteFragment, "Place Autocomplete Fragment");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latLng = place.getLatLng();
                placeMarkerTo(latLng, place.getAddress());
                setCamera(latLng, true, false);
            }

            @Override
            public void onError(Status status) {

            }
        });
    }

    private void startIntentService(FetchAddressIntentService.RequestType requestType) {
        if (mLastLocation == null) return;

        synchronized (mLock) {
            mResultMightBePending = true;
        }

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        intent.putExtra(FetchAddressIntentService.Constants.REQUEST_TYPE, requestType);
        startService(intent);
    }

    private void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                mLocationServicesEnabled = true;
            }
            initiateMap();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        mLocationServicesEnabled = true;
                        initiateMap();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(LocationPickerActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            finishWithResult(Activity.RESULT_CANCELED, new HashMap());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        initiateMap();
                        break;
                    default:
                        finishWithResult(Activity.RESULT_CANCELED, new HashMap());
                        break;
                }
            }
        });
    }

    private void startLocationServices() {
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // wait for the location update first
        if (mLastLocation != null) {
            setCamera(mLastLocation, true, true);
            startIntentService(FetchAddressIntentService.RequestType.LOCATION_REQUEST);
        }
    }

    private void placeMarkerTo(LatLng location) {
        final String label = String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude);
        renderMarkerTo(location, label);

        startIntentService(FetchAddressIntentService.RequestType.MARKER_REQUEST);
    }

    private void placeMarkerTo(LatLng location, CharSequence address) {
        renderMarkerTo(location, address);
    }

    private void renderMarkerTo(LatLng location, CharSequence address) {
        removeMarker();
        mMarkerAddress = (String) address;
        mMarker = mMap.addMarker(new MarkerOptions().position(location).title((String)address));
        mLastLocation.setLatitude(location.latitude);
        mLastLocation.setLongitude(location.longitude);
    }

    private void setCamera(Location location, boolean moveToMarker, boolean shouldZoomIn) {
        LatLng lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
        setCamera(lastLocation, moveToMarker, shouldZoomIn);
    }

    private void setCamera(LatLng location, boolean moveToMarker, boolean shouldZoomIn) {
        CameraPosition.Builder positionBuilder = new CameraPosition.Builder();

        if (moveToMarker) {
            positionBuilder.target(location);
        }

        if (shouldZoomIn) {
            positionBuilder.zoom(mMap.getMaxZoomLevel()/2);
        }

        if (moveToMarker || shouldZoomIn) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(positionBuilder.build()));
        }
    }

    private void finishWithResult(int resultCode, HashMap<String, Object> resultSet) {

        Intent resultIntent = new Intent();
        resultIntent.putExtra(LOCATION_RESULT, resultSet);

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent);
        }

        finish();
    }

    private void removeMarker() {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    private void setProgress(boolean on) {
        if (on) {
            mProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            FetchAddressIntentService.RequestType requestType = (FetchAddressIntentService.RequestType)
                    resultData.getSerializable(FetchAddressIntentService.Constants.REQUEST_TYPE);
            boolean markerRequest = requestType == FetchAddressIntentService.RequestType.MARKER_REQUEST;

            if(resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                String resultAddress = resultData.getString(FetchAddressIntentService.Constants.ADDRESS_RESULT_KEY);
                if (mMarker != null && markerRequest) {
                    mMarkerAddress = resultAddress;
                    mMarker.setTitle(mMarkerAddress);
                } else {
                    mLocationAddress = resultAddress;
                }
            } else {
                String latLngString = String.valueOf(mLastLocation.getLatitude()) + ", " +
                        String.valueOf(mLastLocation.getLongitude());

                if (mMarker != null && markerRequest) {
                    mMarkerAddress = latLngString;
                } else {
                    mLocationAddress = latLngString;
                }
            }
            synchronized(mLock) {
                mResultMightBePending = false;
            }
        }
    }
}
