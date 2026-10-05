package ir.dorvishi.color;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private static final int FILE_CHOOSER = 1001;
    private static final int CAMERA_PERMISSION = 1002;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        webView.setBackgroundColor(0xFFF5F7FB);
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r){ return false; }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams params){
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = cb;
                Intent i = params.createIntent();
                try { startActivityForResult(i, FILE_CHOOSER); }
                catch (Exception e) { uploadCallback = null; return false; }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        webView.loadUrl("file:///android_asset/index.html");
    }

    public void printCurrentPage(View ignored){
        PrintManager pm=(PrintManager)getSystemService(PRINT_SERVICE);
        pm.print("تشخیص رنگ درویشی", webView.createPrintDocumentAdapter("رسید تشخیص رنگ"), new PrintAttributes.Builder().build());
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==FILE_CHOOSER && uploadCallback!=null){
            Uri[] results=null;
            if(resultCode==RESULT_OK && data!=null){
                if(data.getClipData()!=null){
                    int n=data.getClipData().getItemCount(); results=new Uri[n];
                    for(int j=0;j<n;j++) results[j]=data.getClipData().getItemAt(j).getUri();
                } else if(data.getData()!=null) results=new Uri[]{data.getData()};
            }
            uploadCallback.onReceiveValue(results); uploadCallback=null;
        }
    }
    @Override public void onBackPressed(){ if(webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
