package com.learnenglishthroughtamil.free;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private TextToSpeech textToSpeech;

    private static final String WEBSITE_URL =
            "https://learnenglishthroughtamilfree.blogspot.com/2026/09/basic-spoken-english-day-1-20.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSplash();

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // No network permission check.
        // No ConnectivityManager.
        // Open WebView directly after splash.
        new Handler().postDelayed(() -> {
            try {
                showWebView();
            } catch (Throwable e) {
                showErrorScreen(
                        "App error: " + e.getClass().getSimpleName()
                );
            }
        }, 1500);
    }

    // ---------------------------------------------------------
    // SPLASH SCREEN
    // ---------------------------------------------------------

    private void showSplash() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        android.widget.ImageView logo =
                new android.widget.ImageView(this);

        logo.setImageResource(R.drawable.app_icon);
        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        180,
                        180
                );

        logoParams.gravity = android.view.Gravity.CENTER;
        layout.addView(logo, logoParams);

        TextView title = new TextView(this);

        title.setText("Learn English Through Tamil");
        title.setTextSize(22);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 25, 0, 0);

        layout.addView(title);

        setContentView(layout);
    }

    // ---------------------------------------------------------
    // WEBVIEW
    // ---------------------------------------------------------

    @SuppressLint("SetJavaScriptEnabled")
    private void showWebView() {

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);

        // Native Android TTS bridge
        // webView.addJavascriptInterface(
//     new AndroidTTS(),
//     "AndroidTTS"
// );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                installAudioBridge();
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {

                if (request.isForMainFrame()) {
                    showOfflineScreen();
                }
            }

            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(
                    WebView view,
                    int errorCode,
                    String description,
                    String failingUrl
            ) {
                showOfflineScreen();
            }
        });

        setContentView(webView);

        // Open the website directly.
        webView.loadUrl(WEBSITE_URL);
    }

    // ---------------------------------------------------------
    // JAVASCRIPT AUDIO BRIDGE
    // ---------------------------------------------------------

    private void installAudioBridge() {

        if (webView == null) {
            return;
        }

        String script =
                "javascript:(function(){" +

                "if(window.__nativeAudioInstalled){return;}" +

                "window.__nativeAudioInstalled=true;" +

                "if(!window.speechSynthesis){return;}" +

                "var originalSpeak=window.speechSynthesis.speak;" +

                "window.speechSynthesis.speak=function(utterance){" +

                "try{" +

                "var text=utterance.text||'';" +
                "var lang=utterance.lang||'';" +

                "if(lang.toLowerCase().indexOf('ta')===0){" +
                "window.AndroidTTS.speakTamil(text);" +
                "}else{" +
                "window.AndroidTTS.speakEnglish(text);" +
                "}" +

                "}catch(e){" +
                "try{originalSpeak.call(window.speechSynthesis,utterance);}catch(x){}" +
                "}" +

                "};" +

                "window.speechSynthesis.cancel=function(){" +
                "try{window.AndroidTTS.stop();}catch(e){}" +
                "};" +

                "})();";

        webView.evaluateJavascript(script, null);
    }

    // ---------------------------------------------------------
    // NATIVE TTS BRIDGE
    // ---------------------------------------------------------

    public class AndroidTTS {

        @JavascriptInterface
        public void speakTamil(String text) {

            if (text == null || text.trim().isEmpty()) {
                return;
            }

            runOnUiThread(() -> {

                if (textToSpeech == null) {
                    return;
                }

                textToSpeech.stop();

                int result = textToSpeech.setLanguage(
                        new Locale("ta", "IN")
                );

                if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {

                    textToSpeech.setSpeechRate(0.85f);

                    textToSpeech.speak(
                            text,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "tamil_audio"
                    );
                }
            });
        }

        @JavascriptInterface
        public void speakEnglish(String text) {

            if (text == null || text.trim().isEmpty()) {
                return;
            }

            runOnUiThread(() -> {

                if (textToSpeech == null) {
                    return;
                }

                textToSpeech.stop();

                Locale selectedLocale =
                        new Locale("en", "IN");

                int result =
                        textToSpeech.setLanguage(selectedLocale);

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    result = textToSpeech.setLanguage(
                            Locale.US
                    );
                }

                if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {

                    textToSpeech.setSpeechRate(0.88f);

                    textToSpeech.speak(
                            text,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "english_audio"
                    );
                }
            });
        }

        @JavascriptInterface
        public void stop() {

            runOnUiThread(() -> {

                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            });
        }
    }

    // ---------------------------------------------------------
    // OFFLINE SCREEN
    // ---------------------------------------------------------

    private void showOfflineScreen() {

        runOnUiThread(() -> {

            LinearLayout layout =
                    new LinearLayout(this);

            layout.setOrientation(
                    LinearLayout.VERTICAL
            );

            layout.setGravity(
                    android.view.Gravity.CENTER
            );

            layout.setPadding(
                    40,
                    40,
                    40,
                    40
            );

            android.widget.ImageView logo =
                    new android.widget.ImageView(this);

            logo.setImageResource(
                    R.drawable.app_icon
            );

            logo.setAdjustViewBounds(true);

            LinearLayout.LayoutParams logoParams =
                    new LinearLayout.LayoutParams(
                            150,
                            150
                    );

            logoParams.gravity =
                    android.view.Gravity.CENTER;

            layout.addView(
                    logo,
                    logoParams
            );

            TextView message =
                    new TextView(this);

            message.setText(
                    "Internet connection is required.\n\n" +
                    "Please connect to the internet and try again."
            );

            message.setTextSize(18);

            message.setGravity(
                    android.view.Gravity.CENTER
            );

            message.setPadding(
                    0,
                    25,
                    0,
                    25
            );

            layout.addView(message);

            Button retry =
                    new Button(this);

            retry.setText("Retry");

            retry.setOnClickListener(v -> {

                showWebView();
            });

            layout.addView(retry);

            setContentView(layout);
        });
    }

    // ---------------------------------------------------------
    // ERROR SCREEN
    // ---------------------------------------------------------

    private void showErrorScreen(String errorMessage) {

        runOnUiThread(() -> {

            LinearLayout layout =
                    new LinearLayout(this);

            layout.setOrientation(
                    LinearLayout.VERTICAL
            );

            layout.setGravity(
                    android.view.Gravity.CENTER
            );

            layout.setPadding(
                    40,
                    40,
                    40,
                    40
            );

            android.widget.ImageView logo =
                    new android.widget.ImageView(this);

            logo.setImageResource(
                    R.drawable.app_icon
            );

            logo.setAdjustViewBounds(true);

            LinearLayout.LayoutParams logoParams =
                    new LinearLayout.LayoutParams(
                            150,
                            150
                    );

            logoParams.gravity =
                    android.view.Gravity.CENTER;

            layout.addView(
                    logo,
                    logoParams
            );

            TextView title =
                    new TextView(this);

            title.setText("App Error");
            title.setTextSize(22);
            title.setGravity(
                    android.view.Gravity.CENTER
            );

            layout.addView(title);

            TextView error =
                    new TextView(this);

            error.setText(errorMessage);
            error.setTextSize(16);
            error.setGravity(
                    android.view.Gravity.CENTER
            );

            error.setPadding(
                    0,
                    20,
                    0,
                    20
            );

            layout.addView(error);

            Button retry =
                    new Button(this);

            retry.setText("Retry");

            retry.setOnClickListener(v -> {

                showWebView();
            });

            layout.addView(retry);

            setContentView(layout);
        });
    }

    // ---------------------------------------------------------
    // BACK BUTTON
    // ---------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (webView != null &&
                webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    // ---------------------------------------------------------
    // CLEANUP
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        if (webView != null) {

            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
