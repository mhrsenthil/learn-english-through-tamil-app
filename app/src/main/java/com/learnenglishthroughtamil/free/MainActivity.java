package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
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
    private boolean ttsReady = false;

    private static final String WEBSITE =
            "https://learnenglishthroughtamilfree.blogspot.com/2026/09/basic-spoken-english-day-1-20.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeTTS();
        showSplash();
    }

    private void showSplash() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("Learn English Through Tamil");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        setContentView(layout);

        new Handler().postDelayed(() -> {
            checkInternetAndOpen();
        }, 1500);
    }

    private void initializeTTS() {

        textToSpeech = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int result = textToSpeech.setLanguage(Locale.ENGLISH);

                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }

    private void speakTamil(String text) {

        if (!ttsReady || textToSpeech == null) {
            return;
        }

        textToSpeech.setLanguage(new Locale("ta", "IN"));
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

        textToSpeech.setLanguage(Locale.ENGLISH);
        textToSpeech.setSpeechRate(0.88f);

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "EnglishSpeech"
        );
    }

    private void checkInternetAndOpen() {

        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager)
                        getSystemService(CONNECTIVITY_SERVICE);

        boolean connected = false;

        if (cm != null) {

            android.net.Network network = cm.getActiveNetwork();

            if (network != null) {

                android.net.NetworkCapabilities capabilities =
                        cm.getNetworkCapabilities(network);

                if (capabilities != null) {

                    connected =
                            capabilities.hasCapability(
                                    android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
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

    private void showWebView() {

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(view, url);

                installAudioBridge(view);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {
                    showOfflineScreen();
                }
            }
        });

        setContentView(webView);

        webView.loadUrl(WEBSITE);
    }

    private void installAudioBridge(WebView view) {

        view.addJavascriptInterface(
                new AndroidTTSBridge(),
                "AndroidTTS"
        );

        String javascript =

                "(function() {" +

                "if (window.__LET_AUDIO_FIXED__) return;" +
                "window.__LET_AUDIO_FIXED__ = true;" +

                /* Tamil button */
                "window.letSpeakTamil = function() {" +
                "  var q = document.getElementById('letQuestion');" +
                "  if (q && window.AndroidTTS) {" +
                "    AndroidTTS.speak(q.innerText || q.textContent, 'ta');" +
                "  }" +
                "};" +

                /* English button */
                "window.letSpeakEnglish = function() {" +
                "  var correct = document.querySelector('#letOptions .let-option.correct');" +
                "  if (correct && window.AndroidTTS) {" +
                "    AndroidTTS.speak(correct.innerText || correct.textContent, 'en');" +
                "  }" +
                "};" +

                /* Detect answer and speak automatically */
                "var options = document.getElementById('letOptions');" +

                "if (options) {" +

                "  var observer = new MutationObserver(function() {" +

                "    var correct = options.querySelector('.let-option.correct');" +

                "    if (correct && window.AndroidTTS) {" +

                "      var answer = (correct.innerText || correct.textContent).trim();" +

                "      if (answer && options.getAttribute('data-spoken-answer') !== answer) {" +

                "        options.setAttribute('data-spoken-answer', answer);" +

                "        setTimeout(function() {" +

                "          AndroidTTS.speak('Correct answer is ' + answer, 'en');" +
                "        }, 150);" +
                "      }" +
                "    }" +

                "  });" +

                "  observer.observe(options, {" +
                "    subtree: true," +
                "    attributes: true," +
                "    attributeFilter: ['class']" +
                "  });" +
                "}" +

                "})();";

        view.evaluateJavascript(javascript, null);
    }

    private void showOfflineScreen() {

        if (webView != null) {

            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.setVisibility(View.GONE);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        TextView message = new TextView(this);

        message.setText(
                "இணைய இணைப்பு இல்லை.\n\n" +
                "Internet connection-ஐ சரிபார்த்து மீண்டும் முயற்சி செய்யுங்கள்."
        );

        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);

        Button retry = new Button(this);
        retry.setText("மீண்டும் முயற்சி");

        retry.setOnClickListener(v -> checkInternetAndOpen());

        layout.addView(message);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = 30;

        layout.addView(retry, buttonParams);

        setContentView(layout);
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }

    public class AndroidTTSBridge {

        @android.webkit.JavascriptInterface
        public void speak(String text, String language) {

            runOnUiThread(() -> {

                if (text == null || text.trim().isEmpty()) {
                    return;
                }

                if ("ta".equals(language)) {
                    speakTamil(text);
                } else {
                    speakEnglish(text);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void stop() {

            runOnUiThread(() -> {

                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            });
        }
    }
}
