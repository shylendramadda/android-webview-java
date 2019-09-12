package com.geeklabs.sevabharathi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.geeklabs.sevabharathi.utils.ConnectivityReceiver;

/**
 * Created by Shylendra Madda on 04/5/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static ProgressDialog progressDialog;
    private static AlertDialog alertDialog;
    private WebView webView;
    private static final String TAG = "MainActivity";
    private boolean doubleBackToExitPressedOnce = false;
    private static int OPEN_SETTINGS_ACTIVITY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        webView = findViewById(R.id.webView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (ConnectivityReceiver.isNetworkAvailable(this)) {
            loadWebView();
        } else {
            showNoInternetDialog(this);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        WebSettings webViewSettings = webView.getSettings();
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        progressDialog = showProgressDialog(this, "Loading..", true);

        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webViewSettings.setSupportMultipleWindows(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webViewSettings.setSupportZoom(true);
        webViewSettings.setBuiltInZoomControls(true);
        webViewSettings.setDisplayZoomControls(false);

        // to save it in offline
        webViewSettings.setAppCachePath(this.getApplicationContext().getCacheDir().getAbsolutePath());
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAppCacheEnabled(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default

        webViewSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        String webURL = "http://www.sevabharathi.org/";
        webView.loadUrl(webURL);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
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
            // set a title for the progress bar
            progressDialog.setTitle(title);
            // set a message for the progress bar
            progressDialog.setMessage("Please wait...");
            //set the progress bar not isCancelable on users' touch
            progressDialog.setCancelable(isCancelable);
            // show the progress bar
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
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

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
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
}
