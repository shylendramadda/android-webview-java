package com.geeklabs.webview.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.geeklabs.webview.R;
import com.geeklabs.webview.utils.ConnectivityReceiver;
import com.geeklabs.webview.utils.Utils;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by Shylendra Madda on 04/5/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static ProgressDialog progressDialog;
    private static AlertDialog alertDialog;
    private static final String TAG = "MainActivity";
    private boolean doubleBackToExitPressedOnce = false;
    private static int OPEN_SETTINGS_ACTIVITY = 5;
    private static int REQUEST_CODE = 1;
    //    String webURL = "https://app.vobo.ai/#/login";
    String webURL = "http://assetmgmt.vobo.ai/#/login/";
    private AdvancedWebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        checkStoragePermissionGranted();

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void checkStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            if (requestCode == REQUEST_CODE) {
                Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
            }
        } else {
            Utils.showToast(this, "Permission is revoked");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        WebSettings webViewSettings = webView.getSettings();
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSettings.setSupportMultipleWindows(true);
        webViewSettings.setSupportZoom(true);
        webViewSettings.setBuiltInZoomControls(true);
        webViewSettings.setDisplayZoomControls(false);
        // to save it in offline
        webViewSettings.setAppCachePath(this.getApplicationContext().getCacheDir().getAbsolutePath());
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAppCacheEnabled(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default
        webViewSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webView.loadUrl(webURL);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressDialog = showProgressDialog(view.getContext(), "Loading..", true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    hideProgressDialog(progressDialog);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                hideProgressDialog(progressDialog);
            }
        });
    }

    public static ProgressDialog showProgressDialog(Context context, String title, boolean isCancelable) {
        try {
            // create a progress bar while the video file is loading
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(title);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(isCancelable);
            progressDialog.show();

            return progressDialog;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static void hideProgressDialog(ProgressDialog progressDialog) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        closeApp();
    }

    private void closeApp() {
        if (doubleBackToExitPressedOnce) {
            finish();
        }

        this.doubleBackToExitPressedOnce = true;
        Utils.showToast(this, "Please click BACK again to exit");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public static void showNoInternetDialog(final Activity context) {
        try {
            alertDialog = new AlertDialog.Builder(context)
                    .setMessage("No internet available. Click OK to go settings, enable WiFi")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent callWiFiSettingIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            context.startActivityForResult(callWiFiSettingIntent, OPEN_SETTINGS_ACTIVITY);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                            context.finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_SETTINGS_ACTIVITY) {
            if (ConnectivityReceiver.isNetworkAvailable(this)) {
                loadWebView();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    onBackPressed();
                }
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectivityReceiver.isNetworkAvailable(this)) {
            loadWebView();
            webView.onResume();
        } else {
            showNoInternetDialog(this);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
