package com.quemb.qmbform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import net.nightwhistler.htmlspanner.HtmlSpanner;

/**
 * Created by ljdelavega on 22.09.2016.
 */
public class FormTriStateCheckFieldCell extends FormBaseCell {

    private TextView mTextView;
    private CheckBox mFalseCheckBox;
    private CheckBox mTrueCheckBox;

    public FormTriStateCheckFieldCell(Context context,
                                      RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {

        super.init();

        mTextView = (TextView) findViewById(R.id.textView);
        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);

        mFalseCheckBox = (CheckBox) findViewById(R.id.falseCheckBox);
        setStyleId(mFalseCheckBox, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);

        mTrueCheckBox = (CheckBox) findViewById(R.id.trueCheckBox);
        setStyleId(mTrueCheckBox, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);

        mFalseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // unchecked to checked
                if (isChecked) {
                    mTrueCheckBox.setChecked(!isChecked);
                    onValueChanged(new Value<Boolean>(!isChecked));
                }
                // checked to unchecked (blank)
                else {
                    onValueChanged(new Value<Boolean>(null));
                }
            }
        });

        mTrueCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // unchecked to checked
                if (isChecked) {
                    mFalseCheckBox.setChecked(!isChecked);
                    onValueChanged(new Value<Boolean>(isChecked));
                }
                // checked to unchecked (blank)
                else {
                    onValueChanged(new Value<Boolean>(null));
                }
            }
        });
    }

    @Override
    protected int getResource() {
        return R.layout.tristate_check_field_cell;
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

        mTextView.setText(finalTitle);
        if (getRowDescriptor().getDisabled())
        {
            mTrueCheckBox.setEnabled(false);
            mTrueCheckBox.setAlpha(0.4f);
            mFalseCheckBox.setEnabled(false);
            mFalseCheckBox.setAlpha(0.4f);
            setTextColor(mTextView, CellDescriptor.COLOR_LABEL_DISABLED);
        }
        else {
            mTrueCheckBox.setEnabled(true);
            mFalseCheckBox.setEnabled(true);
        }

        @SuppressWarnings("unchecked") Value<Boolean> value = (Value<Boolean>) getRowDescriptor().getValue();
        if (value != null && value.getValue() != null) {
            mFalseCheckBox.setChecked(!value.getValue());
            mTrueCheckBox.setChecked(value.getValue());
        }
    }

    public CheckBox getCheckBox() {
        return mTrueCheckBox;
    }

    public View getEditorView() {
        return mTrueCheckBox;
    }
}
