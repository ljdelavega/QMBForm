package com.quemb.qmbform.view;

import com.quemb.qmbform.R;
import com.quemb.qmbform.descriptor.DataSourceListener;
import com.quemb.qmbform.descriptor.RowDescriptor;
import com.quemb.qmbform.descriptor.Value;
import com.quemb.qmbform.exceptions.NoDataSourceException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonimoeckel on 15.07.14.
 */
public class FormTextPickerDialogFieldCell extends FormEditTextFieldCell {

    private ImageButton mImageButton;

    public FormTextPickerDialogFieldCell(Context context,
                                         RowDescriptor rowDescriptor) {
        super(context, rowDescriptor);
    }

    @Override
    protected void init() {

        super.init();
        mImageButton = (ImageButton) findViewById(R.id.imageButton);
        addListenerOnButton();
    }

    @Override
    protected int getResource() {
        return R.layout.text_picker_field_cell;
    }

    public void addListenerOnButton() {
        mImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onCellSelected();
            }
        });

    }

    @Override
    public void onCellSelected() {
        super.onCellSelected();
        if (getRowDescriptor().getDataSource() == null) {
            throw new NoDataSourceException();
        } else {
            getRowDescriptor().getDataSource().loadData(new DataSourceListener() {
                @Override
                public void onDataSourceLoaded(List list) {
                    final CharSequence title = getRowDescriptor().getTitle();
                    CharSequence requiredSuffix = "";
                    if (getRowDescriptor().getRequired())
                    {
                        requiredSuffix = new HtmlSpanner().fromHtml((" <sup><font color='red'>*</font></sup>"));
                    }
                    final CharSequence finalTitle = TextUtils.concat(title, requiredSuffix);

                    if (list.size() > 0) {
                        final ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_selectable_list_item, list);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                onValueChanged(new Value<Object>(adapter.getItem(which)));
                                update();
                                dialog.dismiss();
                            }
                        })
                                .setTitle(finalTitle);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.title_no_entries);
                        builder.setMessage(R.string.msg_no_entries);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }


                }
            });
        }

    }

    public View getEditorView() {
        return null;
    }
}
