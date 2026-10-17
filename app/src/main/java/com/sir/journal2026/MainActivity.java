package com.sir.journal2026;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

/**
 * 日程本 2026 —— 单文件网页日记本的安卓壳。
 * 三件事是普通 WebView 壳必须补上的，缺一个就会坏：
 *   1) 用 WebViewAssetLoader 把 assets 映射成 https 域名，否则 file:// 下 localStorage 被禁，日记存不住；
 *   2) 实现 onShowFileChooser，否则日历格插图的 <input type="file"> 点了没反应；
 *   3) 接管返回键，否则一按就退出。
 */
public class MainActivity extends Activity {

    private static final int REQ_FILE = 1001;
    private static final String HOME = "https://appassets.androidplatform.net/assets/journal.html";

    private WebView web;
    private ValueCallback<Uri[]> filePathCallback;
    private long lastBackAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.setWebContentsDebuggingEnabled(true); // 便于真机远程调试（chrome://inspect）

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);            // localStorage：日记全靠它
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);           // 内容万一超宽时自动缩到一屏内，消除真机横溢
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);                      // 不跟着系统字体缩放走，排版才不会散

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .setDomain("appassets.androidplatform.net")
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;                    // 页面里没有外链，一律交给 WebView
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return loader.shouldInterceptRequest(Uri.parse(url));
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                /* 标记移动端：触发页面里的 .app-mobile 沉浸式样式（仅在壳内生效，桌面浏览器不受影响） */
                view.evaluateJavascript("document.documentElement.classList.add('app-mobile');", null);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                Intent i = params.createIntent();
                if (i.getType() == null) i.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "选一张图片"), REQ_FILE);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "打不开图库", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        web.loadUrl(HOME);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_FILE || filePathCallback == null) return;
        Uri[] picked = null;
        if (res == RESULT_OK && data != null) {
            String one = data.getDataString();
            if (one != null) {
                picked = new Uri[]{ Uri.parse(one) };
            } else if (data.getClipData() != null) {
                int n = data.getClipData().getItemCount();
                picked = new Uri[n];
                for (int i = 0; i < n; i++) picked[i] = data.getClipData().getItemAt(i).getUri();
            }
        }
        filePathCallback.onReceiveValue(picked);   // 取消时给 null，页面才知道没选
        filePathCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) { web.goBack(); return; }
        long now = System.currentTimeMillis();
        if (now - lastBackAt < 2000) {
            super.onBackPressed();
        } else {
            lastBackAt = now;
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.evaluateJavascript("if(typeof save==='function')save();", null);
    }

    @Override
    protected void onDestroy() {
        if (web != null) { web.destroy(); web = null; }
        super.onDestroy();
    }
}
