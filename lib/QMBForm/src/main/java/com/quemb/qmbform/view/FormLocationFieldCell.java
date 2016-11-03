package com.quemb.qmbform.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.IntentStarter;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by saturnaa on 16-09-02.
 */
public class FormLocationFieldCell extends FormBaseCell implements ResultRequestor{
    private static final Logger LOGGER = Logger.getLogger(FormLocationFieldCell.class.getName());

    public final static String ADDRESS_RESULT = "_string";

    private TextView mTextView;
    private TextView mLocationResultView;

    public FormLocationFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected int getResource() {
        return R.layout.location_field_cell;
    }

    @Override
    protected void init() {
        super.init();

        mTextView = (TextView) findViewById(R.id.textView);
        mLocationResultView = (TextView) findViewById(R.id.locationResultView);

        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);
        setStyleId(mLocationResultView, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_LABEL);

        //set listener for image view click
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            launchLocationPicker();
            }
        });
    }

    @Override
    protected void update() {
        CharSequence title = getRowDescriptor().getTitle();

        if (getRowDescriptor().getRequired()) {
            title = new HtmlSpanner().fromHtml((title + " <sup><font color='red'>*</font></sup>"));
        }

        RowDescriptor rd = getRowDescriptor();
        HashMap<String, Object> resultSet = (HashMap<String, Object>) rd.getValue().getValue();

        if (resultSet != null) {
            final String locationText = (String) resultSet.get(ADDRESS_RESULT);
            mLocationResultView.setText(locationText == null ? "" : locationText);
        }

        mTextView.setText(title);
        mTextView.setVisibility(title == null ? GONE : VISIBLE);

        setTextColor(mTextView, getRowDescriptor().getDisabled() ? CellDescriptor.COLOR_LABEL_DISABLED : CellDescriptor.COLOR_LABEL);
        setTextColor(mLocationResultView, getRowDescriptor().getDisabled() ? CellDescriptor.COLOR_LABEL_DISABLED : CellDescriptor.COLOR_LABEL);

        setClickable(!getRowDescriptor().getDisabled());
        setClickable(!getRowDescriptor().getDisabled());

        rd.getSectionDescriptor().getFormDescriptor().getFormManager().updateRows();
    }

    @Override
    public View getEditorView() {
        return mLocationResultView;
    }

    private void launchLocationPicker() {
        final IntentStarter starter = (IntentStarter) getContext();
        final Intent intent = new Intent(getContext(), LocationPickerActivity.class);
        starter.startActivityForResult(this, intent);
    }

    @Override
    public void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                HashMap<String, Object> resultSet = (HashMap) data.getSerializableExtra(LocationPickerActivity.LOCATION_RESULT);
                onValueChanged(new Value<HashMap>(resultSet));
                update();
            }
        }
        else {
            LOGGER.log(Level.SEVERE, "Barcode activity returned with invalid result. Activity Result Code = " + resultCode);
        }
    }
}
