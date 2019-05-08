package de.streblow.ocrscan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private Activity mActivity;
    private boolean previewing = false;
    private boolean focussed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        autoFocusHandler = new Handler();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mPreview = new CameraPreview(this, mCamera, previewCallback, autoFocusCallback);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        mCamera.setPreviewCallback(previewCallback);
        mCamera.startPreview();
        previewing = false;
        focussed = false;
        mCamera.autoFocus(autoFocusCallback);
        mActivity = (Activity)this;
        final ImageButton button1 = (ImageButton) findViewById(R.id.imageButton1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (previewing) {
                    previewing = false;
                    focussed = false;
                } else {
                    previewing = true;
                    focussed = false;
                }
            }
        });
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void onResume() {
        super.onResume();
        previewing = false;
        focussed = false;
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            mPreview = new CameraPreview(this, mCamera, previewCallback, autoFocusCallback);
            FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
            preview.addView(mPreview);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            focussed = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                if (mCamera != null)
                    mCamera.autoFocus(autoFocusCallback);
            } else {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        }
    };

    PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (!previewing)
                return;
            if (focussed) {
                try {
                    camera.cancelAutoFocus();
                    Camera.Parameters parameters = camera.getParameters();
                    Size size = parameters.getPreviewSize();
                    YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                            size.width, size.height, null);
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ocrscan_camera.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()),
                            100, fos);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                previewing = false;
                playBeep();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    };

    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                focussed = true;
            }
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    public void playBeep() {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    }

}
