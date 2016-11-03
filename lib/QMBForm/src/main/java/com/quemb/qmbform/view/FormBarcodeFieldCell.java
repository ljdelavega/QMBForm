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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by saturnaa on 16-09-02.
 */
public class FormBarcodeFieldCell extends FormBaseCell implements ResultRequestor {
    private static final Logger LOGGER = Logger.getLogger(FormBarcodeFieldCell.class.getName());

    private TextView mTextView;
    private TextView mBarcodeResultView;

    public FormBarcodeFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected int getResource() {
        return R.layout.barcode_field_cell;
    }

    @Override
    protected void init() {
        super.init();

        mTextView = (TextView) findViewById(R.id.textView);
        mBarcodeResultView = (TextView) findViewById(R.id.barcodeResultView);

        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);

        //set listener for image view click
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBarcodeScanner();
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
        mBarcodeResultView.setText((CharSequence)rd.getValue().getValue());

        mTextView.setText(title);
        mTextView.setVisibility(title == null ? GONE : VISIBLE);

        setTextColor(mTextView, getRowDescriptor().getDisabled() ? CellDescriptor.COLOR_LABEL_DISABLED : CellDescriptor.COLOR_LABEL);
        setTextColor(mBarcodeResultView, getRowDescriptor().getDisabled() ? CellDescriptor.COLOR_LABEL_DISABLED : CellDescriptor.COLOR_LABEL);

        setClickable(!getRowDescriptor().getDisabled());
        setEnabled(!getRowDescriptor().getDisabled());

        rd.getSectionDescriptor().getFormDescriptor().getFormManager().updateRows();
    }

    @Override
    public View getEditorView() {
        return mBarcodeResultView;
    }

    private void launchBarcodeScanner() {
        final IntentStarter starter = (IntentStarter) getContext();
        final Intent intent = new Intent(getContext(), BarcodeScannerActivity.class);
        starter.startActivityForResult(this, intent);
    }

    @Override
    public void onResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String result = (String) data.getSerializableExtra(BarcodeScannerActivity.BARCODE_RESULT);
                onValueChanged(new Value<String>(result));
                update();
            }
        }
        else {
            LOGGER.log(Level.SEVERE, "Barcode activity returned with invalid result. Activity Result Code = " + resultCode);
        }
    }
}
