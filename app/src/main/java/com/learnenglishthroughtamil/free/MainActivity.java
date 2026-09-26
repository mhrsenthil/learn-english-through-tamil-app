package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String BLOGGER_URL =
            "https://learnenglishthroughtamilfree.blogspot.com/";

    private WebView webView;

    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    private String pendingText = null;
    private String pendingLanguage = "en-IN";

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private boolean pageLoaded = false;


    // =========================================================
    // ACTIVITY START
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Splash
        showSplashScreen();

        // 2. Prepare Native TTS immediately
        initializeTTS();

        // 3. Wait for splash
        mainHandler.postDelayed(() -> {

            // 4. Internet check
            checkInternetAndOpen();

        }, 2500);
    }


    // =========================================================
    // SPLASH SCREEN
    // =========================================================

    private void showSplashScreen() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER
        );

        layout.setBackgroundColor(
                Color.WHITE
        );


        ImageView logo =
                new ImageView(this);

        logo.setImageResource(
                R.drawable.app_icon
        );

        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        220,
                        220
                );

        layout.addView(
                logo,
                logoParams
        );


        TextView title =
                new TextView(this);

        title.setText(
                "Learn English Through Tamil 51"
        );

        title.setTextSize(22);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setTextColor(
                Color.rgb(25, 118, 210)
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                20,
                10,
                10
        );

        layout.addView(title);


        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Learn English Easily"
        );

        subtitle.setTextSize(16);

        subtitle.setTextColor(
                Color.DKGRAY
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        layout.addView(subtitle);


        setContentView(layout);
    }


    // =========================================================
    // INTERNET CHECK
    // =========================================================

    private void checkInternetAndOpen() {

        if (isInternetAvailable()) {

            showWebView();

        } else {

            showOfflineScreen();

        }
    }


    private boolean isInternetAvailable() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager)
                        getSystemService(
                                CONNECTIVITY_SERVICE
                        );

        if (connectivityManager == null) {
            return false;
        }


        Network network =
                connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }


        NetworkCapabilities capabilities =
                connectivityManager
                        .getNetworkCapabilities(network);

        if (capabilities == null) {
            return false;
        }


        return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
                &&
                capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                );
    }


    // =========================================================
    // OFFLINE SCREEN
    // =========================================================

    private void showOfflineScreen() {

        if (webView != null) {

            webView.stopLoading();
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
                35,
                30,
                35,
                30
        );

        layout.setBackgroundColor(
                Color.WHITE
        );


        TextView icon =
                new TextView(this);

        icon.setText("📡");

        icon.setTextSize(60);

        icon.setGravity(
                Gravity.CENTER
        );

        layout.addView(icon);


        TextView title =
                new TextView(this);

        title.setText(
                "No Internet Connection"
        );

        title.setTextSize(24);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setTextColor(
                Color.rgb(30, 30, 30)
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                15,
                10,
                10
        );

        layout.addView(title);


        TextView message =
                new TextView(this);

        message.setText(
                "இந்த app-ஐ பயன்படுத்த Internet connection தேவை.\n\n" +
                "Wi-Fi அல்லது Mobile Data-ஐ ON செய்து மீண்டும் முயற்சி செய்யுங்கள்."
        );

        message.setTextSize(17);

        message.setTextColor(
                Color.DKGRAY
        );

        message.setGravity(
                Gravity.CENTER
        );

        message.setPadding(
                10,
                10,
                10,
                25
        );

        layout.addView(message);


        Button retryButton =
                new Button(this);

        retryButton.setText(
                "🔄 Retry"
        );

        retryButton.setTextSize(16);

        retryButton.setOnClickListener(
                v -> {

                    showSplashScreen();

                    mainHandler.postDelayed(
                            this::checkInternetAndOpen,
                            800
                    );
                }
        );

        layout.addView(
                retryButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );


        setContentView(layout);
    }


    // =========================================================
    // WEBVIEW
    // =========================================================

    private void showWebView() {

        pageLoaded = false;


        webView =
                new WebView(this);


        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setMediaPlaybackRequiresUserGesture(
                false
        );

        settings.setBuiltInZoomControls(false);

        settings.setDisplayZoomControls(false);


        // Native TTS bridge
        webView.addJavascriptInterface(
                new AndroidTTS(),
                "AndroidTTS"
        );


        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {

                        super.onPageFinished(
                                view,
                                url
                        );

                        pageLoaded = true;

                        installNativeSpeechBridge();
                    }


                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceError error
                    ) {

                        super.onReceivedError(
                                view,
                                request,
                                error
                        );

                        if (request.isForMainFrame()) {

                            pageLoaded = false;

                            showOfflineScreen();
                        }
                    }
                }
        );


        webView.loadUrl(
                BLOGGER_URL
        );


        setContentView(webView);


        // Safety timeout
        mainHandler.postDelayed(() -> {

            if (!pageLoaded &&
                    webView != null) {

                showOfflineScreen();
            }

        }, 15000);
    }


    // =========================================================
    // NATIVE TEXT TO SPEECH
    // =========================================================

    private void initializeTTS() {

        textToSpeech =
                new TextToSpeech(
                        this,
                        status -> {

                            if (status ==
                                    TextToSpeech.SUCCESS) {

                                ttsReady = true;


                                textToSpeech
                                        .setSpeechRate(
                                                0.88f
                                        );

                                textToSpeech
                                        .setPitch(
                                                1.0f
                                        );


                                if (
                                        android.os.Build.VERSION.SDK_INT
                                                >= 21
                                ) {

                                    textToSpeech
                                            .setAudioAttributes(
                                                    new AudioAttributes.Builder()
                                                            .setUsage(
                                                                    AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY
                                                            )
                                                            .setContentType(
                                                                    AudioAttributes.CONTENT_TYPE_SPEECH
                                                            )
                                                            .build()
                                            );
                                }


                                // Speak pending request
                                if (pendingText != null) {

                                    String text =
                                            pendingText;

                                    String language =
                                            pendingLanguage;

                                    pendingText = null;

                                    speakNative(
                                            text,
                                            language
                                    );
                                }
                            }
                        }
                );
    }


    // =========================================================
    // SPEAK NATIVE
    // =========================================================

    private void speakNative(
            String text,
            String language
    ) {

        if (text == null ||
                text.trim().isEmpty()) {

            return;
        }


        final String finalText =
                text.trim();


        final String finalLanguage =
                language == null ||
                        language.trim().isEmpty()
                        ? detectLanguage(finalText)
                        : language;


        mainHandler.post(() -> {

            if (!ttsReady ||
                    textToSpeech == null) {

                pendingText =
                        finalText;

                pendingLanguage =
                        finalLanguage;

                return;
            }


            // Stop previous speech
            textToSpeech.stop();


            // Select language
            Locale locale;


            if (finalLanguage
                    .toLowerCase()
                    .startsWith("ta")) {

                locale =
                        Locale.forLanguageTag(
                                "ta-IN"
                        );

            } else {

                locale =
                        Locale.forLanguageTag(
                                "en-IN"
                        );
            }


            int result =
                    textToSpeech.setLanguage(
                            locale
                    );


            // Fallback
            if (result ==
                    TextToSpeech.LANG_MISSING_DATA
                    ||
                    result ==
                            TextToSpeech.LANG_NOT_SUPPORTED) {

                if (finalLanguage
                        .toLowerCase()
                        .startsWith("ta")) {

                    textToSpeech.setLanguage(
                            Locale.forLanguageTag(
                                    "ta"
                            )
                    );

                } else {

                    textToSpeech.setLanguage(
                            Locale.US
                    );
                }
            }


            float speed = 0.88f;

            if (finalLanguage
                    .toLowerCase()
                    .startsWith("ta")) {

                speed = 0.92f;
            }


            textToSpeech.setSpeechRate(
                    speed
            );


            textToSpeech.speak(
                    finalText,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "learn_english_tamil_" +
                            System.currentTimeMillis()
            );
        });
    }


    // =========================================================
    // LANGUAGE DETECTION
    // =========================================================

    private String detectLanguage(
            String text
    ) {

        for (
                int i = 0;
                i < text.length();
                i++
        ) {

            char c =
                    text.charAt(i);


            // Tamil Unicode range
            if (
                    c >= 0x0B80 &&
                    c <= 0x0BFF
            ) {

                return "ta-IN";
            }
        }


        return "en-IN";
    }


    // =========================================================
    // STOP TTS
    // =========================================================

    private void stopNative() {

        mainHandler.post(() -> {

            if (textToSpeech != null) {

                textToSpeech.stop();
            }
        });
    }


    // =========================================================
    // JAVASCRIPT → ANDROID BRIDGE
    // =========================================================

    public class AndroidTTS {

        @JavascriptInterface
        public void speak(
                String text
        ) {

            speakNative(
                    text,
                    detectLanguage(text)
            );
        }


        @JavascriptInterface
        public void speakWithLanguage(
                String text,
                String language
        ) {

            speakNative(
                    text,
                    language
            );
        }


        @JavascriptInterface
        public void stop() {

            stopNative();
        }
    }


    // =========================================================
    // IMPORTANT:
    // CONVERT ALL BLOGGER speechSynthesis TO NATIVE TTS
    // =========================================================

    private void installNativeSpeechBridge() {

        String javascript =

                "(function() {" +

                "if (!window.AndroidTTS) return;" +

                "if (window.__nativeTTSInstalled) return;" +

                "window.__nativeTTSInstalled = true;" +

                "if (!window.speechSynthesis) return;" +

                "var synth = window.speechSynthesis;" +

                "var nativeToken = 0;" +


                // SPEAK
                "synth.speak = function(utterance) {" +

                "  var text = '';" +

                "  var lang = '';" +

                "  if (utterance) {" +

                "    text = utterance.text || '';" +

                "    lang = utterance.lang || '';" +

                "  }" +

                "  if (!text.trim()) return;" +

                "  nativeToken++;" +

                "  var token = nativeToken;" +

                "  AndroidTTS.speakWithLanguage(text, lang);" +

                "  var words = text.trim().split(/\\s+/).length;" +

                "  var delay = Math.max(1000, Math.min(15000, words * 420));" +

                "  setTimeout(function() {" +

                "    if (token !== nativeToken) return;" +

                "    if (utterance && typeof utterance.onend === 'function') {" +

                "      try {" +

                "        utterance.onend({type:'end'});" +

                "      } catch(e) {}" +

                "    }" +

                "  }, delay);" +

                "};" +


                // CANCEL
                "synth.cancel = function() {" +

                "  nativeToken++;" +

                "  AndroidTTS.stop();" +

                "};" +


                // PAUSE
                "synth.pause = function() {" +

                "  AndroidTTS.stop();" +

                "};" +


                // RESUME
                "synth.resume = function() {};" +

                "})();";


        webView.evaluateJavascript(
                javascript,
                null
        );
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
}
