package com.quemb.qmbform.view;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.SignatureValue;
import com.quemb.qmbform.descriptor.Value;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lester Dela Vega (ljdelavega) on 08/11/2016
 */
public class FormSignatureFieldCell extends FormBaseCell {

    private TextView mTextView;

    private ImageView mImageView;
    private EditText mName;
    private EditText mDate;


    public FormSignatureFieldCell(Context context,
                                  RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {

        super.init();
        mTextView = (TextView) findViewById(R.id.textView);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mName = (EditText) findViewById(R.id.nameText);
        mDate = (EditText) findViewById(R.id.dateText);
        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);
        setStyleId(mName, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_LABEL);
        setStyleId(mDate, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_LABEL);

        //set listener for image view click
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signatureIntent = new Intent(getContext(), SignatureActivity.class);
                getContext().startActivity(signatureIntent);
                }
        });

    }

    @Override
    protected int getResource() {
        return R.layout.signature_field_cell;
    }

    @Override
    protected void update() {

        String title = getFormItemDescriptor().getTitle();
        mTextView.setText(title);
        mTextView.setVisibility(title == null ? GONE : VISIBLE);

        @SuppressWarnings("unchecked") Value<SignatureValue> value = (Value<SignatureValue>) getRowDescriptor().getValue();
        if (value == null || value.getValue() == null) {
            value = new Value<SignatureValue>(new SignatureValue() );
        } else {
            updateNameLabel(value.getValue().getName(), getRowDescriptor().getDisabled());
            updateDateLabel(value.getValue().getDate(), getRowDescriptor().getDisabled());
        }

        final Calendar calendar = Calendar.getInstance();
        Date date = value.getValue().getDate();
        calendar.setTime(date);

        initDatePicker(calendar);

        if (getRowDescriptor().getDisabled())
        {
            setTextColor(mTextView, CellDescriptor.COLOR_LABEL_DISABLED);

            setClickable(false);
            setEnabled(false);
        }

    }

    protected void initDatePicker(Calendar calendar) {

    }

    public void onNameChanged(String name) {

        updateNameLabel(name, false);

        onValueChanged(new Value<String>(name));

    }

    protected void updateNameLabel(String name, boolean disabled) {
        mName.setText(name);

        if (disabled)
        {
            mName.setEnabled(false);
            setTextColor(mName, CellDescriptor.COLOR_VALUE_DISABLED);
        }

    }

    public void onDateChanged(Date date) {

        updateDateLabel(date, false);

        onValueChanged(new Value<Date>(date));

    }

    protected void updateDateLabel(Date date, boolean disabled) {

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        String s = dateFormat.format(date);

        mDate.setText(s);

        if (disabled)
        {
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
}
