package com.quemb.qmbform.descriptor;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by ljdelavega on 16-08-15.
 */
public class SignatureValue {

    private String name;
    private Date date;
    private Bitmap signature;

    public SignatureValue() {
        this.name = "";
        this.date = new Date();
        this.signature = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    public SignatureValue(String name, Date date, Bitmap signature) {
        this.name = name;
        this.date = date;
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Bitmap getSignature() {
        return signature;
    }

    public void setSignature(Bitmap signature) {
        this.signature = signature;
    }


}
