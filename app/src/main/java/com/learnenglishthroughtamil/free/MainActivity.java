package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    private static final String WEBSITE =
            "https://learnenglishthroughtamilfree.blogspot.com/2026/09/basic-spoken-english-day-1-20.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeTTS();
        showSplash();
    }

    // =========================================================
    // SPLASH SCREEN
    // =========================================================

    private void showSplash() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        // Your original app icon
        ImageView logo = new ImageView(this);

        logo.setImageResource(R.drawable.app_icon);
        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        180,
                        180
                );

        logoParams.bottomMargin = 25;

        layout.addView(logo, logoParams);

        // App title
        TextView title = new TextView(this);

        title.setText("Learn English Through Tamil");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        setContentView(layout);

        // Splash duration
        new Handler().postDelayed(() -> {

            checkInternetAndOpen();

        }, 1500);
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    private void initializeTTS() {

        textToSpeech = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int result =
                        textToSpeech.setLanguage(Locale.ENGLISH);

                ttsReady =
                        result != TextToSpeech.LANG_MISSING_DATA
                                &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }

    private void speakTamil(String text) {

        if (!ttsReady || textToSpeech == null) {
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        textToSpeech.stop();

        int result =
                textToSpeech.setLanguage(
                        new Locale("ta", "IN")
                );

        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

            // Fallback
            textToSpeech.setLanguage(
                    new Locale("ta")
            );
        }

        textToSpeech.setSpeechRate(0.85f);

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "TamilSpeech"
        );
    }

    private void speakEnglish(String text) {

        if (!ttsReady || textToSpeech == null) {
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        textToSpeech.stop();

        int result =
                textToSpeech.setLanguage(
                        Locale.ENGLISH
                );

        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {

            textToSpeech.setLanguage(
                    Locale.US
            );
        }

        textToSpeech.setSpeechRate(0.88f);

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "EnglishSpeech"
        );
    }

    // =========================================================
    // INTERNET CHECK
    // =========================================================

    private void checkInternetAndOpen() {

        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(CONNECTIVITY_SERVICE);

        boolean connected = false;

        if (cm != null) {

            Network network =
                    cm.getActiveNetwork();

            if (network != null) {

                NetworkCapabilities capabilities =
                        cm.getNetworkCapabilities(network);

                if (capabilities != null) {

                    connected =
                            capabilities.hasCapability(
                                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                            );
                }
            }
        }

        if (connected) {

            showWebView();

        } else {

            showOfflineScreen();
        }
    }

    // =========================================================
    // WEBVIEW
    // =========================================================

    private void showWebView() {

        webView = new WebView(this);

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setSupportZoom(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        // IMPORTANT:
        // Add Android TTS BEFORE loading the webpage.
        webView.addJavascriptInterface(
                new AndroidTTSBridge(),
                "AndroidTTS"
        );

        webView.setWebChromeClient(
                new WebChromeClient()
        );

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url) {

                        super.onPageFinished(view, url);

                        installAudioBridge(view);
                    }

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceError error) {

                        super.onReceivedError(
                                view,
                                request,
                                error
                        );

                        if (request.isForMainFrame()) {

                            showOfflineScreen();
                        }
                    }
                }
        );

        setContentView(webView);

        // Load website AFTER AndroidTTS bridge exists
        webView.loadUrl(WEBSITE);
    }

    // =========================================================
    // AUDIO BRIDGE
    // =========================================================

    private void installAudioBridge(WebView view) {

        String javascript =

                "(function() {" +

                "if (window.__ANDROID_TTS_FIXED__) return;" +

                "window.__ANDROID_TTS_FIXED__ = true;" +

                "function install() {" +

                "  try {" +

                "    if (!window.speechSynthesis) {" +
                "      setTimeout(install, 300);" +
                "      return;" +
                "    }" +

                "    var originalSpeak = " +
                "        window.speechSynthesis.speak;" +

                "    var originalCancel = " +
                "        window.speechSynthesis.cancel;" +

                "    if (window.__ANDROID_TTS_PATCHED__) return;" +

                "    window.__ANDROID_TTS_PATCHED__ = true;" +

                "    window.speechSynthesis.speak = function(utterance) {" +

                "      try {" +

                "        var text = '';" +
                "        var lang = 'en';" +

                "        if (utterance) {" +
                "          text = utterance.text || '';" +
                "          lang = utterance.lang || 'en';" +
                "        }" +

                "        if (text && window.AndroidTTS) {" +

                "          if (lang.toLowerCase().indexOf('ta') === 0) {" +
                "            window.AndroidTTS.speak(text, 'ta');" +
                "          } else {" +
                "            window.AndroidTTS.speak(text, 'en');" +
                "          }" +

                "          return;" +
                "        }" +

                "      } catch(e) {}" +

                "      try {" +
                "        originalSpeak.call(window.speechSynthesis, utterance);" +
                "      } catch(e) {}" +
                "    };" +

                "    window.speechSynthesis.cancel = function() {" +

                "      try {" +
                "        if (window.AndroidTTS) {" +
                "          window.AndroidTTS.stop();" +
                "        }" +
                "      } catch(e) {}" +

                "      try {" +
                "        originalCancel.call(window.speechSynthesis);" +
                "      } catch(e) {}" +
                "    };" +

                "  } catch(e) {" +
                "    setTimeout(install, 300);" +
                "  }" +
                "}" +

                "install();" +

                "})();";

        view.evaluateJavascript(
                javascript,
                null
        );
    }

    // =========================================================
    // OFFLINE SCREEN
    // =========================================================

    private void showOfflineScreen() {

        if (webView != null) {

            webView.stopLoading();

            webView.setVisibility(View.GONE);

            webView.destroy();

            webView = null;
        }

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER
        );

        layout.setPadding(
                40,
                40,
                40,
                40
        );

        // Logo
        ImageView logo =
                new ImageView(this);

        logo.setImageResource(
                R.drawable.app_icon
        );

        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        140,
                        140
                );

        logoParams.bottomMargin = 25;

        layout.addView(
                logo,
                logoParams
        );

        // Message
        TextView message =
                new TextView(this);

        message.setText(
                "இணைய இணைப்பு இல்லை.\n\n" +
                "Internet connection-ஐ சரிபார்த்து\n" +
                "மீண்டும் முயற்சி செய்யுங்கள்."
        );

        message.setTextSize(18);

        message.setGravity(
                Gravity.CENTER
        );

        layout.addView(message);

        // Retry button
        Button retry =
                new Button(this);

        retry.setText(
                "மீண்டும் முயற்சி"
        );

        retry.setOnClickListener(
                v -> checkInternetAndOpen()
        );

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        retryParams.topMargin = 30;

        layout.addView(
                retry,
                retryParams
        );

        setContentView(layout);
    }

    // =========================================================
    // BACK BUTTON
    // =========================================================

    @Override
    public void onBackPressed() {

        if (webView != null &&
                webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

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

    // =========================================================
    // ANDROID TTS JAVASCRIPT BRIDGE
    // =========================================================

    public class AndroidTTSBridge {

        @JavascriptInterface
        public void speak(
                String text,
                String language) {

            runOnUiThread(() -> {

                if (text == null ||
                        text.trim().isEmpty()) {

                    return;
                }

                if ("ta".equalsIgnoreCase(language)) {

                    speakTamil(text);

                } else {

                    speakEnglish(text);
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
}
