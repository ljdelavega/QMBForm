package com.quemb.qmbform.view;

import android.content.Context;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.RowDescriptor;

/**
 * This cell gives the user a chance to handle the form row click without
 * having to disable the row
 */
public class FormExternalPickerInlineFieldCell extends FormDetailTextInlineFieldCell {

    public FormExternalPickerInlineFieldCell(Context context,
                                             RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected int getResource() {
        return R.layout.external_picker_field_cell;
    }
}
