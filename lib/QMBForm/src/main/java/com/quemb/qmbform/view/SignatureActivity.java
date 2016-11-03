package com.quemb.qmbform.view;

/**
 * Created by delavegal on 16-08-15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.quemb.qmbform.R;

import net.nightwhistler.htmlspanner.HtmlSpanner;

public class SignatureActivity extends Activity {
    private static final Logger LOGGER = Logger.getLogger(SignatureActivity.class.getName());

    public static final String SIGNATURE_NAME = "signatureName";
    public static final String SIGNATURE_DATE = "signatureDate";
    public static final String SIGNATURE_BASE64 = "signatureBase64";

    private static final String SIGNATURE_SAVED_STATE = "signatureSavedState";
    private static final String SIGNATURE_SAVED_HEIGHT = "signatureSavedHeight";
    private static final String SIGNATURE_SAVED_WIDTH = "signatureSavedWidth";

    private TextView mDisclaimerField;
    private SignaturePad mSignaturePad;
    private EditText mNameField;
    private TextView mDateField;
    private Button mClearButton;
    private Button mSaveButton;

    private String mDisclaimer;
    private String mSavedSignatureState;
    private int mSavedHeight;
    private int mSavedWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signature_activity);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mClearButton.setEnabled(false);
            }
        });

        mDisclaimerField = (TextView) findViewById(R.id.disclaimer);
        mNameField = (EditText) findViewById(R.id.name_field);
        mDateField = (TextView) findViewById(R.id.date_field);

        final Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("disclaimer") ) {
                mDisclaimer = intent.getStringExtra("disclaimer");
                mDisclaimerField.setText(new HtmlSpanner().fromHtml(mDisclaimer));
            }
        }

        // Set date to today's date.
        Date date = new Date();
        final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        mDateField.setText(df.format(date));

        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
                mNameField.setText("");
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSignaturePad.isEmpty()) {
                    if (mNameField.getText().length() < 1) {
                        new AlertDialog.Builder(SignatureActivity.this)
                                .setTitle(R.string.signature_name_required)
                                .setMessage(R.string.signature_name_required_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing, just dismiss.
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    else {
                        //Encode bitmap to Base64 string and save everything in intent extras to be returned to cell.
                        Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap(true);
                        final String base64Signature = encodeBitmapToString(signatureBitmap);

                        // Set date to today's date.
                        Date date = new Date();
                        final long dateLong = date.getTime();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(SIGNATURE_NAME, mNameField.getText().toString());
                        resultIntent.putExtra(SIGNATURE_DATE, dateLong);
                        resultIntent.putExtra(SIGNATURE_BASE64, base64Signature);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                }
                else {
                    // send null result back to cell.
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!mSignaturePad.isEmpty()) {
            //Encode bitmap to Base64 string and save in bundle to be restored later.
            Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap(true);
            if (signatureBitmap != null) {
                String base64Signature = encodeBitmapToString(signatureBitmap);
                int height = signatureBitmap.getHeight();
                int width = signatureBitmap.getWidth();

                outState.putString(SIGNATURE_SAVED_STATE, base64Signature);
                outState.putInt(SIGNATURE_SAVED_HEIGHT, height);
                outState.putInt(SIGNATURE_SAVED_WIDTH, width);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SIGNATURE_SAVED_STATE)) {
            mSavedSignatureState = savedInstanceState.getString(SIGNATURE_SAVED_STATE);
            mSavedWidth = savedInstanceState.getInt(SIGNATURE_SAVED_WIDTH);
            mSavedHeight = savedInstanceState.getInt(SIGNATURE_SAVED_HEIGHT);

            final byte[] decodedBytes = Base64.decode(mSavedSignatureState, Base64.DEFAULT);
            Bitmap savedSignature = decodeSampledBitmapFromResource(decodedBytes, mSavedWidth, mSavedHeight);

            mSignaturePad.setSignatureBitmap(savedSignature);
        }
    }

    private String encodeBitmapToString(Bitmap bitmap) {
        final ByteArrayOutputStream signatureOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, signatureOutputStream);
        return Base64.encodeToString(signatureOutputStream.toByteArray(), Base64.DEFAULT);
    }

    /**
     * Helper function to decode a bitmap from a byte array and scale it down to specified req width and height.
     * See https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
     * @param data
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(byte[] data, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeByteArray(data, 0, data.length, onlyBoundsOptions);

        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // Calculate inSampleSize
        bitmapOptions.inSampleSize = calculateInSampleSize(onlyBoundsOptions, reqWidth, reqHeight);
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
    }

    private int calculateInSampleSize (BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
