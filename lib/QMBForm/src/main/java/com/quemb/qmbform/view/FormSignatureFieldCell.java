package com.quemb.qmbform.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.IntentStarter;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Signature;
import com.quemb.qmbform.descriptor.Value;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lester Dela Vega (ljdelavega) on 08/11/2016
 */
public class FormSignatureFieldCell extends FormBaseCell implements ResultRequestor {
    private static final Logger LOGGER = Logger.getLogger(FormSignatureFieldCell.class.getName());

    public static final String CELL_CONFIG_SIGNATURE_DISCLAIMER = "SignatureField.Disclaimer";

    private TextView mTextView;
    private LinearLayout mSignatureView;
    private ImageView mImageView;
    private TextView mName;
    private TextView mDate;


    public FormSignatureFieldCell(Context context,
                                  RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {

        super.init();
        mTextView = (TextView) findViewById(R.id.textView);
        mSignatureView = (LinearLayout) findViewById(R.id.signatureView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mName = (TextView) findViewById(R.id.nameText);
        mDate = (TextView) findViewById(R.id.dateText);
        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);
        setStyleId(mName, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_LABEL);
        setStyleId(mDate, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_LABEL);

        //set listener for image view click
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignatureActivity();
            }
        });

    }

    @Override
    protected int getResource() {
        return R.layout.signature_field_cell;
    }

    @Override
    protected void update() {
        final CharSequence title = getRowDescriptor().getTitle();
        CharSequence requiredSuffix = "";
        if (getRowDescriptor().getRequired())
        {
            requiredSuffix = new HtmlSpanner().fromHtml((" <sup><font color='red'>*</font></sup>"));
        }
        final CharSequence finalTitle = TextUtils.concat(title, requiredSuffix);

        mTextView.setText(finalTitle);
        mTextView.setVisibility(title == null ? GONE : VISIBLE);

        @SuppressWarnings("unchecked") Value<Signature> value = (Value<Signature>) getRowDescriptor().getValue();
        if (value == null || value.getValue() == null) {
            value = new Value<Signature>(new Signature() );
        } else {
            final Signature signature = (Signature) getRowDescriptor().getValue().getValue();

            updateNameLabel(signature.getName(), getRowDescriptor().getDisabled());
            final String dateString = signature.getDate();
            final DateFormat internalDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date signatureDate;
            try {
                signatureDate = internalDateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Unable to parse signature date", e);
                signatureDate = new Date();
            }
            updateDateLabel(signatureDate, getRowDescriptor().getDisabled());

            final String base64Thumbnail = signature.getBase64Signature();
            final byte[] decodedBytes = Base64.decode(base64Thumbnail, Base64.DEFAULT);

            // Get screen width and height to scale thumbnail to 1/2 the size of the screen
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            int reqWidth = metrics.widthPixels / 4;
            int reqHeight = metrics.heightPixels / 4;

            final Bitmap thumbnail = decodeSampledBitmapFromResource(decodedBytes, reqWidth, reqHeight);

            mImageView.setImageBitmap(thumbnail);
        }

        if (getRowDescriptor().getDisabled())
        {
            setTextColor(mTextView, CellDescriptor.COLOR_LABEL_DISABLED);
            setClickable(false);
            setEnabled(false);
        }
    }

    @Override
    public View getEditorView() {
        return mSignatureView;
    }

    protected void updateNameLabel(String name, boolean disabled) {
        mName.setText(name);

        if (disabled)
        {
            mName.setEnabled(false);
            setTextColor(mName, CellDescriptor.COLOR_VALUE_DISABLED);
        }

    }

    protected void updateDateLabel(Date date, boolean disabled) {

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        String s = dateFormat.format(date);

        mDate.setText(s);

        if (disabled) {
            mDate.setEnabled(false);
            setTextColor(mDate, CellDescriptor.COLOR_VALUE_DISABLED);
        }

    }

    @Override
    public void onCellSelected() {
        super.onCellSelected();

    }

    public TextView getTextView() {
        return mTextView;
    }

    private void launchSignatureActivity() {
        final IntentStarter starter = (IntentStarter) getContext();
        final Intent intent = new Intent(getContext(), SignatureActivity.class);

        // get disclaimer from cell config if it exists.
        final HashMap<String, Object> config = getRowDescriptor().getCellConfig();
        if (config != null) {
            if (config.containsKey(CELL_CONFIG_SIGNATURE_DISCLAIMER)) {
                final String disclaimer = (String) config.get(CELL_CONFIG_SIGNATURE_DISCLAIMER);
                intent.putExtra("disclaimer", disclaimer);
            }
        }

        starter.startActivityForResult(this, intent);
    }

    @Override
    public void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data.hasExtra(SignatureActivity.SIGNATURE_NAME)) {
                final String signatureName = (String) data.getSerializableExtra(SignatureActivity.SIGNATURE_NAME);
                final Long dateLong = (Long) data.getSerializableExtra(SignatureActivity.SIGNATURE_DATE);
                final Date date = new Date(dateLong);
                final DateFormat internalDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                final String signatureDate = internalDateFormat.format(date).toString();
                final String signatureBase64 = (String) data.getSerializableExtra(SignatureActivity.SIGNATURE_BASE64);

                final Signature signature = new Signature(signatureName, signatureDate, signatureBase64);
                onValueChanged(new Value<Signature>(signature));
                update();
            }
            else {
                // null result, clear the value
                onValueChanged(new Value<Signature>(null));
                update();
            }
        }
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
