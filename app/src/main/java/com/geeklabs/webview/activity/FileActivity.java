package com.geeklabs.webview.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.geeklabs.webview.R;
import com.github.barteksc.pdfviewer.PDFView;

public class FileActivity extends AppCompatActivity {

    private static final String TAG = "FileActivity";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        TextView fileNameTV = findViewById(R.id.fileNameTV);
        ImageView imageView = findViewById(R.id.imageView);
        PDFView pdfView = findViewById(R.id.pdfView);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action != null && action.equals(Intent.ACTION_SEND) && type != null) {
            if (type.startsWith("text/")) {
                String stringExtra = intent.getStringExtra("android.intent.extra.TEXT");
                if (!TextUtils.isEmpty(stringExtra)) {
                    fileNameTV.setText(stringExtra);
                    Log.d(TAG, stringExtra);
                }
            } else if (type.startsWith("image/")) {
                Uri receiveUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (receiveUri != null) {
                    String imagePath = receiveUri.toString();
                    imageView.setImageURI(receiveUri);
                    fileNameTV.setText(imagePath);
                    Log.d(TAG, imagePath);
                }
            } else if (type.startsWith("application/pdf")) {
                Uri receiveUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (receiveUri != null) {
                    String realPath = receiveUri.toString();
                    fileNameTV.setText(realPath);
                    Log.d(TAG, "" + realPath);
                    pdfView.fromUri(receiveUri).load();
                }
            }
        }
    }
}

