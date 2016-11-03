package com.quemb.qmbform.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.vision.barcode.Barcode;
import com.quemb.qmbform.R;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by saturnaa on 16-09-08.
 */
public class TrackerView extends View {

    private static final double HALF = 0.5;
    private static final double TENTH = 0.1;
    private static final double NINTH = 0.9;
    private static final int CROSS_OFFSET = 40;

    private static final Logger LOGGER = Logger.getLogger("TrackerView");

    private int mColor;
    private Float mFrameWidth;
    private Paint mFramePaint;
    private Paint mCrosshairPaint;
    private Barcode mBarcode;
    private int mPreviewWidth;
    private int mPreviewHeight;

    public TrackerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TrackerView,
                0, 0);

        try {
            mColor = a.getColor(R.styleable.TrackerView_frame_color, Color.GREEN);
            mFrameWidth = a.getFloat(R.styleable.TrackerView_frame_width, 20.0f);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFramePaint.setColor(mColor);
        mFramePaint.setStrokeWidth(mFrameWidth);

        mCrosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCrosshairPaint.setColor(mColor);
        mCrosshairPaint.setStrokeWidth(mFrameWidth * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBarcode != null) {
            Rect barcodeRect = mBarcode.getBoundingBox();

            float scaleX = (float) canvas.getWidth() / mPreviewWidth;
            float scaleY = (float) canvas.getHeight() / mPreviewHeight;


            scaleRect(barcodeRect, scaleX, scaleY);

            float[] points = {
                    barcodeRect.left, barcodeRect.top,
                    barcodeRect.right, barcodeRect.top,
                    barcodeRect.right, barcodeRect.top,
                    barcodeRect.right, barcodeRect.bottom,
                    barcodeRect.right, barcodeRect.bottom,
                    barcodeRect.left, barcodeRect.bottom,
                    barcodeRect.left, barcodeRect.bottom,
                    barcodeRect.left, barcodeRect.top};

            canvas.drawLines(points, mFramePaint);
        }

        float[] crosshair = {
                Math.round(canvas.getWidth() * TENTH), Math.round(canvas.getHeight() * HALF),
                Math.round(canvas.getWidth() * NINTH), Math.round(canvas.getHeight() * HALF),
                Math.round(canvas.getWidth() * HALF), Math.round(canvas.getHeight() * HALF) + CROSS_OFFSET,
                Math.round(canvas.getWidth() * HALF), Math.round(canvas.getHeight() * HALF) - CROSS_OFFSET
        };

        canvas.drawLines(crosshair, mCrosshairPaint);
    }

    public void setPreviewSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }

    public void setBarcode(Barcode barcode) {
        mBarcode = barcode;
    }

    private void scaleRect(Rect rect, float scaleX, float scaleY) {
        rect.left *= scaleX;
        rect.right *= scaleX;
        rect.top *= scaleY;
        rect.bottom *= scaleY;
    }
}
