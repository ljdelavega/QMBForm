package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.TextUtil;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormTitleFieldCell extends FormBaseCell {
    private TextView mTextView;

    public FormTitleFieldCell(Context context,
                              RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {
        super.init();
        mTextView = (TextView) findViewById(R.id.textView);

        setStyleId(mTextView, CellDescriptor.APPEARANCE_TEXT_LABEL, CellDescriptor.COLOR_LABEL);
    }

    @Override
    protected int getResource() {
        return R.layout.text_field_cell;
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
        mTextView.setVisibility(title == null ? GONE : VISIBLE);


        if (getRowDescriptor().getDisabled()) {
            getRowDescriptor().setOnFormRowClickListener(null);

            setTextColor(mTextView, CellDescriptor.COLOR_LABEL_DISABLED);
        }
    }

    public TextView getTextView() {
        return mTextView;
    }

    public View getEditorView() {
        return null;
    }
}
