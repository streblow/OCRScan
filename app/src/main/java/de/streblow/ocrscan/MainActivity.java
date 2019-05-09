package de.streblow.ocrscan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static boolean RUN_ONCE = true;

    public final String savedStateFilename = "savedState";

    private Bitmap m_Bitmap;
    private String m_BitmapFilename;
    private CropImageView m_CropImageView;
    private TesseractOCR m_TesseractOCR;
    private ProgressDialog m_ProgressDialog;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    int PERMISSION_ALL = 1;
    boolean flagPermissions = false;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_CropImageView = (CropImageView) findViewById(R.id.cropImageView1);
        m_CropImageView.setScaleType(CropImageView.ScaleType.CENTER_INSIDE);
        m_CropImageView.setAutoZoomEnabled(true);
        m_CropImageView.setShowProgressBar(false);
        m_CropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                Bitmap bmp = view.getCroppedImage();
                doOCR(bmp);
            }
        });
        if (!flagPermissions) {
            checkPermissions();
        }
        String language = "deu";
        m_TesseractOCR = new TesseractOCR(this, language);
        m_Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        m_BitmapFilename = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/ocrscan_camera.jpg";
        m_CropImageView.setImageBitmap(m_Bitmap);
        if (RUN_ONCE) {
            File file = new File(m_BitmapFilename);
            if (file.exists()) {
                file.delete();
            }
            RUN_ONCE = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                return true;
            case R.id.action_help:
                HelpDialog help = new HelpDialog(this);
                help.setTitle(R.string.help_title);
                help.show();
                return true;
            case R.id.action_about:
                AboutDialog about = new AboutDialog(this);
                about.setTitle(R.string.about_title);
                about.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreState();
    }

    public void saveState() {
        DataOutputStream dos = null;
        try {
            File file = new File(getFilesDir(), savedStateFilename);
            dos = new DataOutputStream(new FileOutputStream(file));
            dos.writeUTF(m_BitmapFilename);
            dos.close();
            dos = null;
        } catch (Exception e) {
            e.printStackTrace();
            if (dos != null)
                try {
                    dos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
        }
    }

    public void restoreState() {
        DataInputStream dis = null;
        try {
            File file = new File(getFilesDir(), savedStateFilename);
            dis = new DataInputStream(new FileInputStream(file));
            m_BitmapFilename = dis.readUTF();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            File test = new File(m_BitmapFilename);
            if (test.exists()) {
                m_Bitmap = BitmapFactory.decodeFile(m_BitmapFilename, options);
                m_CropImageView.setImageBitmap(m_Bitmap);
            } else {
                m_Bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                m_CropImageView.setImageBitmap(m_Bitmap);
            }
            dis.close();
            dis = null;
        } catch (Exception e) {
            e.printStackTrace();
            if (dis != null)
                try {
                    dis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
        }
    }

    public void button1Click(View view) {
        OpenFileDialog dlg = new OpenFileDialog(this, new OpenFileDialog.OpenFileDialogListener() {
            @Override
            public void onChosenDir(String chosenFile) {
                if (chosenFile != "") {
                    String bitmap_file_path = chosenFile.replace("//", "/");
                    File imgFile = new  File(bitmap_file_path);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    for (options.inSampleSize = 1; options.inSampleSize <= 16; options.inSampleSize++) {
                        try {
                            m_Bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                            break;
                        } catch (OutOfMemoryError outOfMemoryError) {
                        }
                    }
                    m_CropImageView.setImageBitmap(m_Bitmap);
                }
            }
        });
        dlg.Default_File_Name = "";
        dlg.chooseFile();
    }

    public void button2Click(View view) {
        Intent takePictureIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void button3Click(View view) {
        m_CropImageView.getCroppedImageAsync();
    }

    public void buttonRotateCCW(View view) {
        m_CropImageView.setRotatedDegrees(m_CropImageView.getRotatedDegrees() - 90);
    }

    public void buttonRotateCW(View view) {
        m_CropImageView.setRotatedDegrees(m_CropImageView.getRotatedDegrees() + 90);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    m_Bitmap = BitmapFactory.decodeFile(m_BitmapFilename, options);
                    m_CropImageView.setImageBitmap(m_Bitmap);
                }
            }
        }
    }

    private void doOCR(final Bitmap bitmap) {
        if (m_ProgressDialog == null) {
            m_ProgressDialog = ProgressDialog.show(this, "Processing",
                    "Doing OCR...", true);
        } else {
            m_ProgressDialog.show();
        }
        new Thread(new Runnable() {
            public void run() {
                final String srcText = m_TesseractOCR.getOCRResult(bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_ProgressDialog.dismiss();
                        if (srcText != null && !srcText.equals("")) {
                            showDialog("Scanned text", srcText);
                        }
                    }
                });
            }
        }).start();
    }

    void showDialog(String title, String text) {
        TextDialog dlg = new TextDialog(this, text);
        dlg.setTitle("OCR result");
        dlg.show();
    }

    void checkPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS,
                        PERMISSION_ALL);
            }
            flagPermissions = false;
        }
        flagPermissions = true;

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
