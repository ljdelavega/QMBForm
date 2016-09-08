package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import net.nightwhistler.htmlspanner.HtmlSpanner;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormCheckFieldCell extends FormBaseCell {

    private CheckBox mCheckBox;

    public FormCheckFieldCell(Context context,
                              RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {

        super.init();

        mCheckBox = (CheckBox) findViewById(R.id.checkBox);
        setStyleId(mCheckBox, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onValueChanged(new Value<Boolean>(isChecked));
            }
        });

    }

    @Override
    protected int getResource() {
        return R.layout.check_field_cell;
    }

    @Override
    protected void update() {
        final CharSequence title = getRowDescriptor().getTitle();
        CharSequence requiredSuffix = "";
        if (getRowDescriptor().getRequired())
        {
            requiredSuffix = new HtmlSpanner().fromHtml((" <sup><font color='red'>*</font></sup>"));
        }
        final CharSequence finalTitle = TextUtils.concat(title, requiredSuffix);

        mCheckBox.setText(finalTitle);
        if (getRowDescriptor().getDisabled())
        {
            mCheckBox.setEnabled(false);
            setTextColor(mCheckBox, CellDescriptor.COLOR_LABEL_DISABLED);
        }
        else
            mCheckBox.setEnabled(true);

        @SuppressWarnings("unchecked") Value<Boolean> value = (Value<Boolean>) getRowDescriptor().getValue();
        if (value != null) {
            mCheckBox.setChecked(value.getValue());
        }

    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }

    public View getEditorView() {
        return mCheckBox;
    }
}
