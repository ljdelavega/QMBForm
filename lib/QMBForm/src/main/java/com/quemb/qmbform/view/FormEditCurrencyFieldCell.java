package com.quemb.qmbform.view;

import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormEditCurrencyFieldCell extends FormEditNumberFieldCell {

    public FormEditCurrencyFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    protected String valueToFormattedText(Value<?> value) {
        final String text;
        if (value == null || value.getValue() == null) {
            text = "";
        } else {
            text = NumberFormat.getCurrencyInstance().format(value.getValue());
        }
        return text;
    }

}
