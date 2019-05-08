package de.streblow.ocrscan;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

public class TesseractOCR {

    private final String tesseractDir = "/tesseract";
    private final String tessdataPath = tesseractDir + "/tessdata/";
    private final String tessdatafile = ".traineddata";

    private final TessBaseAPI m_TessBaseAPI;

    public TesseractOCR(Context context, String language) {
        m_TessBaseAPI = new TessBaseAPI();
        boolean fileExistFlag = false;
        AssetManager assetManager = context.getAssets();
        String dstPathDir = tessdataPath;
        String srcFile = language + tessdatafile;
        InputStream inFile = null;
        dstPathDir = context.getFilesDir() + dstPathDir;
        String dstInitPathDir = context.getFilesDir() + tesseractDir;
        String dstPathFile = dstPathDir + srcFile;
        FileOutputStream outFile = null;
        File file = new File(dstPathFile);
        if (file.exists())
            m_TessBaseAPI.init(dstInitPathDir, language);
        else {
            try {
                inFile = assetManager.open(srcFile);
                File f = new File(dstPathDir);
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        Toast.makeText(context, srcFile + " can't be created.", Toast.LENGTH_SHORT).show();
                    }
                    outFile = new FileOutputStream(new File(dstPathFile));
                } else {
                    fileExistFlag = true;
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                if (fileExistFlag) {
                    try {
                        if (inFile != null) inFile.close();
                        m_TessBaseAPI.init(dstInitPathDir, language);
                        return;
                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                if (inFile != null && outFile != null) {
                    try {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inFile.read(buf)) != -1) {
                            outFile.write(buf, 0, len);
                        }
                        inFile.close();
                        outFile.close();
                        m_TessBaseAPI.init(dstInitPathDir, language);
                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, srcFile + " can't be read.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public String getOCRResult(Bitmap bitmap) {
        m_TessBaseAPI.setImage(bitmap);
        return m_TessBaseAPI.getUTF8Text();
    }

    public void onDestroy() {
        if (m_TessBaseAPI != null) m_TessBaseAPI.end();
    }
}
