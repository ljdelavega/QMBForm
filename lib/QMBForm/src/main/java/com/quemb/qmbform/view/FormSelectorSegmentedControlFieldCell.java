package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.FormOptionsObject;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by pmaccamp on 9/8/2015.
 */
public class FormSelectorSegmentedControlFieldCell extends FormBaseCell {
    private SegmentedGroup mSegmentedGroup;
    private TextView mTextView;
    private ArrayList<Object> mValues;

    public FormSelectorSegmentedControlFieldCell(Context context, RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {
        super.init();
        mValues = new ArrayList<Object>();
        mSegmentedGroup = (SegmentedGroup) findViewById(R.id.segmentedGroup);
        mTextView = (TextView) findViewById(R.id.textView);

        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_VALUE, CellDescriptor.COLOR_VALUE);
    }

    @Override
    protected int getResource() {
        return R.layout.selector_segmented_control_field_cell;
    }

    @Override
    protected void update() {
        mSegmentedGroup.removeAllViews();

        final CharSequence title = getRowDescriptor().getTitle();
        CharSequence requiredSuffix = "";
        if (getRowDescriptor().getRequired())
        {
            requiredSuffix = new HtmlSpanner().fromHtml((" <sup><font color='red'>*</font></sup>"));
        }
        final CharSequence finalTitle = TextUtils.concat(title, requiredSuffix);
        mTextView.setText(finalTitle);

        List<FormOptionsObject> selectorOptions = getRowDescriptor().getSelectorOptions();
        for (FormOptionsObject option : selectorOptions) {
            addButton(mSegmentedGroup,
                    mValues.size(),
                    option.getDisplayText(),
                    option.getValue().equals(getRowDescriptor().getValueData()));
            mValues.add(option.getValue());
        }

        mSegmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId >= 0 && checkedId < mValues.size()) {
                    onValueChanged(new Value<>(mValues.get(checkedId)));
                }
            }
        });
    }

    public View getEditorView() {
        return mSegmentedGroup;
    }

    private void addButton(SegmentedGroup group, int id, String displayText, boolean checked) {
        RadioButton radioButton = new RadioButton(getContext(), null, R.style.RadioButton);
        radioButton.setLayoutParams(new SegmentedGroup.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1));
        radioButton.setText(displayText);
        radioButton.setId(id);
        radioButton.setGravity(Gravity.CENTER_HORIZONTAL);
        radioButton.setClickable(true);
        radioButton.setChecked(checked);
        int padding = getResources().getDimensionPixelOffset(R.dimen.qmb_padding);
        radioButton.setPadding(padding, padding, padding, padding);
        group.addView(radioButton);
        group.updateBackground();
    }
}
