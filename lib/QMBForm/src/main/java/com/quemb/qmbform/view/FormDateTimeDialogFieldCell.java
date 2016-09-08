package com.quemb.qmbform.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.RowDescriptor;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ljdelavega on 09.07.2016.
 */
public class FormDateTimeDialogFieldCell extends FormDateFieldCell implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Calendar mCalendar;

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
        datePickerDialog.show();

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(year, monthOfYear, dayOfMonth);

        // After getting the date value from the date picker dialog, open a time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
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
