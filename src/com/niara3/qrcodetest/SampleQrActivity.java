package com.niara3.qrcodetest;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

// http://9ensan.com/blog/smartphone/android/android-qr-zxing-sample/
// https://github.com/pikanji/CameraPreviewSample
public class SampleQrActivity extends Activity implements AutoFocusCallback, PreviewCallback {

    private CameraPreview mPreview;
    private FrameLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sample_qr);
        mLayout = (FrameLayout) findViewById(R.id.viewCameraParent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the second argument by your choice.
        // Usually, 0 for back-facing camera, 1 for front-facing camera.
        // If the OS is pre-gingerbreak, this does not have any effect.
        mPreview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
        FrameLayout.LayoutParams previewLayoutParams = new FrameLayout.LayoutParams(
        		FrameLayout.LayoutParams.WRAP_CONTENT,
        		FrameLayout.LayoutParams.WRAP_CONTENT);
        // Un-comment below lines to specify the size.
        //previewLayoutParams.height = 500;
        //previewLayoutParams.width = 500;

        // Un-comment below line to specify the position.
        //mPreview.setCenterPosition(270, 130);
        
        mLayout.addView(mPreview, 0, previewLayoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mLayout.removeView(mPreview); // This is necessary.
        mPreview = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    	if (mPreview == null) {
    		return;
    	}
        Result rawResult = null;
        Camera.Size previewSize = mPreview.getPreviewSize();
        float previewWidthRatio = mPreview.getPreviewWidthRatio();
        float previewHeightRatio = mPreview.getPreviewHeightRatio();
        View target = (View) findViewById(R.id.target);
        int left = (int) (target.getLeft() * previewWidthRatio);
        int top = (int) (target.getTop() * previewHeightRatio);
        int width = (int) (target.getWidth() * previewWidthRatio);
        int height = (int) (target.getHeight() * previewHeightRatio);
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, previewSize.width,
                previewSize.height, left, top, width, height, false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            try {
                rawResult = multiFormatReader.decode(bitmap);
                Toast.makeText(getApplicationContext(), rawResult.getText(), Toast.LENGTH_LONG)
                        .show();
            } catch (ReaderException re) {
                Toast.makeText(getApplicationContext(), "read error: " + re.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
        	mPreview.setOneShotPreviewCallback(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPreview != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            	mPreview.autoFocus(this);
            }
        }
        return super.onTouchEvent(event);
    }
}
