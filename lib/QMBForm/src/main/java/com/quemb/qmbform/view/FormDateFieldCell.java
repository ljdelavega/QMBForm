package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.CellDescriptor;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormDateFieldCell extends FormDetailTextInlineFieldCell {

    public final static String CELL_CONFIG_DATE_FORMAT = "EditText.dateFormat";
    public final static String CELL_CONFIG_DATE_TIME_FORMAT = "EditText.dateTimeFormat";

    private TextView mTextView;

    public FormDateFieldCell(Context context,
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
        return R.layout.date_field_cell;
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

        @SuppressWarnings("unchecked") Value<Date> value = (Value<Date>) getRowDescriptor().getValue();
        if (value == null || value.getValue() == null) {
            value = new Value<Date>(new Date());
        } else {
            updateDateLabel(value.getValue(), getRowDescriptor().getDisabled());
        }

        final Calendar calendar = Calendar.getInstance();
        Date date = value.getValue();
        calendar.setTime(date);

        initDatePicker(calendar);

        if (getRowDescriptor().getDisabled())
        {
            setTextColor(mTextView, CellDescriptor.COLOR_LABEL_DISABLED);

            setClickable(false);
            setEnabled(false);
        }

    }

    protected void initDatePicker(Calendar calendar) {
        // used in child classes
    }

    public void onDateChanged(Date date) {
        updateDateLabel(date, false);

        onValueChanged(new Value<Date>(date));
    }

    protected void updateDateLabel(Date date, boolean disabled) {
        TextView editView = getDetailTextView();

        if (date == null) {
            editView.setText("");
        }
        else {
            String label = DateFormat.getDateInstance().format(date);

            final HashMap<String, Object> config = getRowDescriptor().getCellConfig();
            if (config != null) {
                if (config.containsKey(CELL_CONFIG_DATE_FORMAT)) {
                    final DateFormat dateFormat = (DateFormat) config.get(CELL_CONFIG_DATE_FORMAT);
                    label = dateFormat.format(date);
                }

                if (config.containsKey(CELL_CONFIG_DATE_TIME_FORMAT)) {
                    final DateFormat dateTimeFormat = (DateFormat) config.get(CELL_CONFIG_DATE_TIME_FORMAT);
                    label = dateTimeFormat.format(date);
                }
            }

            editView.setText(label);
        }

        if (disabled)
        {
            editView.setEnabled(false);
            setTextColor(editView, CellDescriptor.COLOR_VALUE_DISABLED);
        }

    }

    @Override
    public void onCellSelected() {
        super.onCellSelected();
    }

    public TextView getTextView() {
        return mTextView;
    }
}
