package com.quemb.qmbform.view;

import android.content.Context;
import android.util.Log;

import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormEditIntegerFieldCell extends FormEditNumberFieldCell {
    private static final Logger LOGGER = Logger.getLogger(FormEditIntegerFieldCell.class.getName());

    public FormEditIntegerFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    protected BigInteger textToValue(String text) {
        try {
            return new BigInteger(text);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to convert text to value", e);
            return null;
        }
    }

    @Override
    protected String valueToFormattedText(Value<?> value) {
        final String text;
        if (value == null || value.getValue() == null) {
            text = "";
        } else {
            final DecimalFormat formatter;
            if (pattern != null) {
                formatter = new DecimalFormat(pattern);
            } else {
                formatter = new DecimalFormat("#");
            }
            formatter.setParseBigDecimal(true);
            text = formatter.format(value.getValue());
        }
        return text;
    }
}
