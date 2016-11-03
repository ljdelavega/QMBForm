package com.quemb.qmbform.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;

import com.quemb.qmbform.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by saturnaa on 16-09-20.
 */
public class FetchAddressIntentService extends IntentService {
    private static final Logger LOGGER = Logger.getLogger("FetchAddressIntentService");
    private ResultReceiver mResultReceiver;
    private Location mLocation;
    private RequestType mRequestType;


    public FetchAddressIntentService(String name) {
        super(name);
    }

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ADDRESS_RESULT_KEY, message);
        bundle.putSerializable(Constants.REQUEST_TYPE, mRequestType);
        mResultReceiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Address> addresses = null;
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        mResultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        // Get the location passed to this service through an extra.
        mLocation = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        mRequestType = (RequestType) intent.getSerializableExtra(Constants.REQUEST_TYPE);

        try {
            addresses = geocoder.getFromLocation(
                    mLocation.getLatitude(),
                    mLocation.getLongitude(),
                    1);

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            LOGGER.log(Level.SEVERE, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            LOGGER.log(Level.SEVERE, errorMessage + ". " +
                    "Latitude = " + mLocation.getLatitude() +
                    ", Longitude = " +
                    mLocation.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                LOGGER.log(Level.SEVERE, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            LOGGER.log(Level.SEVERE, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String REQUEST_TYPE = "request_type";
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String ADDRESS_RESULT_KEY = PACKAGE_NAME +
                ".ADDRESS_RESULT_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }

    public enum RequestType {
        MARKER_REQUEST,
        LOCATION_REQUEST
    }
}
