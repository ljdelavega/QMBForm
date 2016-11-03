package com.quemb.qmbform.descriptor;

import android.content.Intent;

import com.quemb.qmbform.view.ResultRequestor;

/**
 * Created by denisp on 16-09-15.
 */
public interface IntentStarter {
    void startActivityForResult(ResultRequestor requestor, Intent intent);
}
