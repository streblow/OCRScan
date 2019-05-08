package de.streblow.ocrscan;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class TextDialog extends Dialog {

	private static Context mContext = null;
	private String mText = "";

	public TextDialog(Context context, String text) {
		super(context);
		mContext = context;
		mText = text;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		EditText edit;
		Button btn;
		setContentView(R.layout.activity_text);
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		edit = (EditText)findViewById(R.id.editText1);
		edit.setText(mText);
		btn = (Button)findViewById(R.id.buttonClipboard);
		btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClipboardClick(v);
            }
        });
    }

	public void buttonClipboardClick(View view) {
		ClipboardManager clipboard;
		ClipData clip;
		clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		clip = ClipData.newPlainText("OCRScan", mText);
		clipboard.setPrimaryClip(clip);
	}

}
