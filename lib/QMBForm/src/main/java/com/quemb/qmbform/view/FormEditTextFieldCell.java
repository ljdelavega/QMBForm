package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.FormItemDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormEditTextFieldCell extends FormTitleFieldCell {

    public final static String CELL_CONFIG_MIN_LINES = "EditText.minLines";
    public final static String CELL_CONFIG_INPUT_TYPE = "EditText.inputType";
    public final static String CELL_CONFIG_GRAVITY = "EditText.gravity";

    private EditText mEditView;

    public FormEditTextFieldCell(Context context,
                                 RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    private Handler handler = new Handler();
    private int lastFocussedPosition = -1;

    @Override
    protected void init() {

        super.init();
        mEditView = (EditText) findViewById(R.id.editText);
        mEditView.setRawInputType(InputType.TYPE_CLASS_TEXT);

        setStyleId(mEditView, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_VALUE);
    }

    @Override
    protected void afterInit() {
        super.afterInit();

        mEditView.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                FormEditTextFieldCell.this.onEditTextChanged(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

    }

    protected void onEditTextChanged(String string) {
        onValueChanged(new Value<String>(string));
    }

    @Override
    protected int getResource() {
        return R.layout.edit_text_field_cell;
    }

    @Override
    protected void update() {

        super.update();


        updateEditView();

        if (getRowDescriptor().getDisabled())
        {
            mEditView.setEnabled(false);
            setTextColor(mEditView, CellDescriptor.COLOR_VALUE_DISABLED);
        }
        else
            mEditView.setEnabled(true);

    }

    protected void updateEditView() {

        String hint = getRowDescriptor().getHint(getContext());
        if (hint != null) {
            mEditView.setHint(hint);
        }

        final int defaultInputType = mEditView.getInputType();
        final HashMap<String, Object> config = getRowDescriptor().getCellConfig();
        if (config != null) {
            if (config.containsKey(CELL_CONFIG_INPUT_TYPE)) {
                final Integer inputType = (Integer) config.get(CELL_CONFIG_INPUT_TYPE);
                mEditView.setInputType(defaultInputType | inputType);
            }

            if (config.containsKey(CELL_CONFIG_MIN_LINES)) {
                final Integer minLines = (Integer) config.get(CELL_CONFIG_MIN_LINES);
                mEditView.setMinLines(minLines);
            }

            if (config.containsKey(CELL_CONFIG_GRAVITY)) {
                final Integer gravity = (Integer) config.get(CELL_CONFIG_GRAVITY);
                mEditView.setGravity(gravity);
            }
        }


        @SuppressWarnings("unchecked") Value<String> value = (Value<String>) getRowDescriptor().getValue();
        if (value != null && value.getValue() != null) {
            String valueString = value.getValue();
            mEditView.setText(valueString);
        }

    }

    public EditText getEditView() {
        return mEditView;
    }

    public View getEditorView() {
        return mEditView;
    }

    /**
     * Set the right side of the TextView to have an error icon and popup message when the field is focused.
     * @param error - String for a message and icon, null to clear the icon and message.
     */
    public void setError(String error)
    {
        mEditView.setError(error);
    }

}
