package com.quemb.qmbform.view;

import android.content.Intent;

/**
 * Created by denisp on 16-09-15.
 */
public interface ResultRequestor {
    void onResult(int resultCode, Intent data);
}
