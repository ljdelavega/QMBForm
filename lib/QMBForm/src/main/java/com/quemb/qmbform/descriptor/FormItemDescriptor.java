package com.quemb.qmbform.descriptor;

import android.text.Html;

import com.quemb.qmbform.OnFormRowClickListener;
import com.quemb.qmbform.view.Cell;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.util.HashMap;

/**
 * Created by tonimoeckel on 14.07.14.
 */
public class FormItemDescriptor {

    protected Cell mCell;

    protected String mTag;


    protected CharSequence mTitle;
    private OnFormRowClickListener mOnFormRowClickListener;
    private HashMap<String, Object> mCellConfig;


    public CharSequence getTitle() {
        if (mTitle != null) {
            return new HtmlSpanner().fromHtml(mTitle.toString());
        }
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public Cell getCell() {
        return mCell;
    }

    public void setCell(Cell cell) {
        mCell = cell;
    }


    public OnFormRowClickListener getOnFormRowClickListener() {
        return mOnFormRowClickListener;
    }

    public void setOnFormRowClickListener(OnFormRowClickListener onFormRowClickListener) {
        mOnFormRowClickListener = onFormRowClickListener;
    }

    public HashMap<String, Object> getCellConfig() {
        return mCellConfig;
    }

    public void setCellConfig(HashMap<String, Object> cellConfig) {
        mCellConfig = cellConfig;
    }
}
