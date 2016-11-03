package com.quemb.qmbform.view;

import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormEditNumberFieldCell extends FormEditTextFieldCell implements View.OnFocusChangeListener {
    private static final Logger LOGGER = Logger.getLogger(FormEditNumberFieldCell.class.getName());

    private static final String DEFAULT_PATTERN = "#,###.##########";
    public final static String CELL_CONFIG_PATTERN = "EditText.pattern";

    protected String pattern;

    protected String oldStringValue;
    protected Object oldValue;

    protected String stringValue;
    protected Object value;

    protected boolean focusChanging = false;

    public FormEditNumberFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {
        super.init();

        final HashMap<String, Object> config = getRowDescriptor().getCellConfig();
        if (config.containsKey(CELL_CONFIG_PATTERN)) {
            this.pattern = (String) config.get(CELL_CONFIG_PATTERN);
        }

        EditText editView = getEditView();
        if (getInputType() != null) {
            editView.setRawInputType(InputType.TYPE_CLASS_NUMBER | getInputType());
        } else {
            editView.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        }
        editView.setMaxLines(1);
        editView.setOnFocusChangeListener(this);
    }

    protected Integer getInputType() {
        return null;
    }

    protected void updateEditView() {
        try {
            final String text = valueToFormattedText(getRowDescriptor().getValue());
            this.stringValue = text;
            this.value = textToValue(stringValue);
            getEditView().setText(text);
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("updateEditView: " + getEditView().hashCode() + " (" + text + ")");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to update EditView", e);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        try {
            focusChanging = true;
            final Value<?> value = getRowDescriptor().getValue();
            final String text;
            if (hasFocus) {
                text = valueToUnformattedText(value);
            } else {
                text = valueToFormattedText(value);
            }
            getEditView().setText(text);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to update cell on focus change", e);

        } finally {
            focusChanging = false;
        }
    }

    protected void onEditTextChanged(String newStringValue) {
        if (focusChanging) return;

        try {
            this.oldStringValue = this.stringValue;
            this.stringValue = newStringValue;

            if (oldStringValue == null || !oldStringValue.equals(newStringValue)) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("onEditTextChanged: " + newStringValue);
                }

                this.oldValue = this.value;
                this.value = textToValue(newStringValue);

                if (oldValue != this.value) {
                    onValueChanged(new Value<>(this.value));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to update cell value", e);
        }
    }

    protected Number textToValue(String text) {
        try {
            return new BigDecimal(text);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to convert text to value", e);
            return null;
        }
    }

    protected String valueToUnformattedText(Value<?> value) {
        final String text;
        if (value == null || value.getValue() == null) {
            text = "";
        } else {
            text = value.getValue().toString();
        }
        return text;
    }

    protected String valueToFormattedText(Value<?> value) {
        final String text;
        if (value == null || value.getValue() == null) {
            text = "";
        } else {
            final DecimalFormat result;
            if (pattern != null) {
                result = new DecimalFormat(pattern);
            } else {
                result = new DecimalFormat(DEFAULT_PATTERN);
            }
            result.setParseBigDecimal(true);
            text = result.format(value.getValue());
        }
        return text;
    }

}
