package com.quemb.qmbform.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.RowDescriptor;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ljdelavega on 09.07.2016.
 */
public class FormDateTimeDialogFieldCell extends FormDateFieldCell implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String CELL_CONFIG_MIN_DATE_MILLIS = "DatePicker.minDate";
    public static final String CELL_CONFIG_MAX_DATE_MILLIS = "DatePicker.maxDate";

    private Calendar mCalendar;

    private Calendar minDate;
    private Calendar maxDate;

    public FormDateTimeDialogFieldCell(Context context,
                                       RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected int getResource() {
        return R.layout.date_field_cell;
    }

    @Override
    protected void initDatePicker(Calendar calendar) {
        mCalendar = calendar;

        final HashMap<String, Object> config = getRowDescriptor().getCellConfig();
        if (config != null && config.containsKey(CELL_CONFIG_MIN_DATE_MILLIS)) {
            minDate = Calendar.getInstance();
            minDate.setTimeInMillis((Long) config.get(CELL_CONFIG_MIN_DATE_MILLIS));
        }
        if (config != null && config.containsKey(CELL_CONFIG_MAX_DATE_MILLIS)) {
            maxDate = Calendar.getInstance();
            maxDate.setTimeInMillis((Long) config.get(CELL_CONFIG_MAX_DATE_MILLIS));
        }
    }

    @Override
    public void onCellSelected() {
        super.onCellSelected();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    //Clear the value.
                    onDateChanged(null);
                }
            }
        });
        if (minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }
        if (maxDate != null) {
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(year, monthOfYear, dayOfMonth);

        // After getting the date value from the date picker dialog, open a time picker dialog
        final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true) {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                super.onTimeChanged(view, hourOfDay, minute);

                boolean valid = true;

                if (minDate != null) {
                    final int minHour = minDate.get(Calendar.HOUR_OF_DAY);
                    final int minMinute = minDate.get(Calendar.MINUTE);

                    if (hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute)) {
                        valid = false;
                    }
                }
                if (maxDate != null) {
                    final int maxHour = maxDate.get(Calendar.HOUR_OF_DAY);
                    final int maxMinute = maxDate.get(Calendar.MINUTE);

                    if (hourOfDay > maxHour || (hourOfDay == maxHour && minute > maxMinute)) {
                        valid = false;
                    }
                }

                final Button positive = getButton(BUTTON_POSITIVE);
                if (positive != null) {
                    positive.setEnabled(valid);
                }
            }
        };
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    //Clear the value.
                    onDateChanged(null);
                }
            }
        });
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, 0);
        onDateChanged(mCalendar.getTime());
    }
}
