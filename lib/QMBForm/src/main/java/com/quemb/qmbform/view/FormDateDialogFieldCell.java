package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.RowDescriptor;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormDateDialogFieldCell extends FormDateFieldCell implements
        DatePickerDialog.OnDateSetListener {

    private Calendar mCalendar;

    public FormDateDialogFieldCell(Context context,
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

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = new Date(calendar.getTimeInMillis());

        onDateChanged(date);
    }
}
