package com.quemb.qmbform.descriptor;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.quemb.qmbform.FormManager;
import com.quemb.qmbform.R;

/**
 * Created by tonimoeckel on 14.07.14.
 */
public class FormDescriptor {
    private String mTitle;
    private HashMap<String, Object> mCellConfig;
    private ArrayList<SectionDescriptor> mSections;
    private OnFormRowValueChangedListener mOnFormRowValueChangedListener;
    private OnFormRowChangeListener mOnFormRowChangeListener;
    private FormManager mFormManager;

    public static FormDescriptor newInstance() {
        return FormDescriptor.newInstance(null);
    }

    public static FormDescriptor newInstance(String title) {

        FormDescriptor descriptor = new FormDescriptor();
        descriptor.mTitle = title;
        return descriptor;

    }

    public FormDescriptor() {
        // set default cell config
        mCellConfig = new HashMap<String, Object>();

        // TextAppearance for section, label, value and button
        mCellConfig.put(CellDescriptor.APPEARANCE_SECTION, Integer.valueOf(R.style.TextAppearance_Form_Section));
        mCellConfig.put(CellDescriptor.APPEARANCE_TEXT_LABEL, Integer.valueOf(R.style.TextAppearance_Form_Label));
        mCellConfig.put(CellDescriptor.APPEARANCE_TEXT_VALUE, Integer.valueOf(R.style.TextAppearance_Form_Value));
        mCellConfig.put(CellDescriptor.APPEARANCE_BUTTON, Integer.valueOf(R.style.TextAppearance_Form_Button));

        // Enabled color for label and value
        // value is color as Integer.valueOf(OxAARRGGBB)
        mCellConfig.put(CellDescriptor.COLOR_LABEL, Integer.valueOf(0xFF000000)); // black
        mCellConfig.put(CellDescriptor.COLOR_VALUE, Integer.valueOf(0xFF000000)); // black

        // Disabled color for label and value
        mCellConfig.put(CellDescriptor.COLOR_LABEL_DISABLED, Integer.valueOf(0xFF999999)); //gray
        mCellConfig.put(CellDescriptor.COLOR_VALUE_DISABLED, Integer.valueOf(0xFF999999)); //gray
        mSections = new ArrayList<SectionDescriptor>();
    }

    /**
     * Set CellConfig member
     */
    public void setCellConfig(HashMap<String, Object> cellConfig) {
        mCellConfig = cellConfig;
    }

    public void setFormManager(FormManager formManager) { mFormManager = formManager; }

    public FormManager getFormManager() { return mFormManager; }

    public void addSection(SectionDescriptor section) {
        insertSectionAtIndex(section, mSections.size());
    }

    public void removeSection(SectionDescriptor sectionDescriptor) {
        int index = mSections.indexOf(sectionDescriptor);
        if (index >= 0) {
            removeSectionAtIndex(index);
        }
    }

    public int countOfSections() {
        return mSections.size();
    }

    public SectionDescriptor sectionAtIndex(int index) {
        if (mSections.size() > index) {
            return mSections.get(index);
        }
        return null;
    }

    public List<SectionDescriptor> getSections() {
        return mSections;
    }

    public SectionDescriptor getSectionWithTitle(String title) {
        for (SectionDescriptor sectionDescriptor : mSections) {
            if (sectionDescriptor.getTitle().equals(title)) {
                return sectionDescriptor;
            }
        }
        return null;
    }

    public void insertSectionAtIndex(SectionDescriptor section, int index) {
        section.setFormDescriptor(this);
        mSections.add(index, section);

        // Propagate the CellConfig from Form to Section

        if (mCellConfig != null)
            section.setCellConfig(mCellConfig);
    }

    private void removeSectionAtIndex(int index) {
        mSections.remove(index);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public OnFormRowValueChangedListener getOnFormRowValueChangedListener() {
        return mOnFormRowValueChangedListener;
    }

    public RowDescriptor findRowDescriptor(String tag) {
        RowDescriptor rowDescriptor = null;

        for (SectionDescriptor sectionDescriptor : getSections()) {
            rowDescriptor = sectionDescriptor.findRowDescriptor(tag);
            if (rowDescriptor != null) break;
        }

        return rowDescriptor;
    }

    public void setOnFormRowValueChangedListener(
            OnFormRowValueChangedListener onFormRowValueChangedListener) {
        mOnFormRowValueChangedListener = onFormRowValueChangedListener;
    }

    public boolean isValid(Context context) {
        FormValidation formValidation = getFormValidation(context);

        return formValidation.getRowValidationErrors().isEmpty();
    }

    public FormValidation getFormValidation(Context context) {
        FormValidation formValidation = new FormValidation(context);
        for (SectionDescriptor sectionDescriptor : getSections()) {
            for (RowDescriptor rowDescriptor : sectionDescriptor.getRows()) {
                if (!rowDescriptor.isValid()) {
                    formValidation.getRowValidationErrors().addAll(rowDescriptor.getValidationErrors());
                }
            }
        }
        return formValidation;

    }

    protected void didInsertRow(RowDescriptor rowDescriptor, SectionDescriptor sectionDescriptor) {
        if (mOnFormRowChangeListener != null) {
            mOnFormRowChangeListener.onRowAdded(rowDescriptor, sectionDescriptor);
        }
    }

    protected void didRemoveRow(RowDescriptor rowDescriptor, SectionDescriptor sectionDescriptor) {
        if (mOnFormRowChangeListener != null) {
            mOnFormRowChangeListener.onRowRemoved(rowDescriptor, sectionDescriptor);
        }
    }


    protected OnFormRowChangeListener getOnFormRowChangeListener() {
        return mOnFormRowChangeListener;
    }

    public void setOnFormRowChangeListener(OnFormRowChangeListener onFormRowChangeListener) {
        mOnFormRowChangeListener = onFormRowChangeListener;
    }

    public Map<String, Object> getFormValues() {
        Map<String, Object> m = new HashMap<String, Object>();
        for (SectionDescriptor section : getSections()) {
            for (RowDescriptor row : section.getRows()) {
                m.put(row.getTag(), row.getValueData());
            }
        }
        return m;
    }
}
