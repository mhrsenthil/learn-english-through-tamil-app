package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Context;
import android.net.ConnectivityManager;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private String pendingText = null;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSplashScreen();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

    if (isInternetAvailable()) {
        showWebView();
    } else {
        showOfflineScreen();
    }

}, 2500);
    }

    private void showSplashScreen() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.learnenglishthroughtamil.free.R.drawable.app_icon);
        logo.setAdjustViewBounds(true);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(
                        220,
                        220
                );

        layout.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("Learn English Through Tamil 01");
        title.setTextSize(22);
        title.setTextColor(Color.rgb(25, 118, 210));
        title.setGravity(Gravity.CENTER);
        title.setPadding(10, 20, 10, 10);

        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Learn English Easily");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);

        layout.addView(subtitle);

        setContentView(layout);
    }

    private boolean isInternetAvailable() {

    ConnectivityManager cm =
            (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE
            );

    if (cm == null) {
        return false;
    }

    android.net.NetworkInfo activeNetwork =
            cm.getActiveNetworkInfo();

    return activeNetwork != null &&
            activeNetwork.isConnected();
}
    private void showOfflineScreen() {

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.CENTER);
    layout.setPadding(40, 30, 40, 30);
    layout.setBackgroundColor(Color.WHITE);

    TextView icon = new TextView(this);
    icon.setText("📡");
    icon.setTextSize(60);
    icon.setGravity(Gravity.CENTER);
    layout.addView(icon);

    TextView title = new TextView(this);
    title.setText("No Internet Connection");
    title.setTextSize(24);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    title.setTextColor(Color.rgb(30, 30, 30));
    title.setGravity(Gravity.CENTER);
    title.setPadding(10, 15, 10, 10);
    layout.addView(title);

    TextView message = new TextView(this);
    message.setText(
            "இந்த app-ஐ பயன்படுத்த Internet connection தேவை.\n\n" +
            "Wi-Fi அல்லது Mobile Data-ஐ ON செய்து மீண்டும் முயற்சி செய்யுங்கள்."
    );
    message.setTextSize(17);
    message.setTextColor(Color.DKGRAY);
    message.setGravity(Gravity.CENTER);
    message.setPadding(10, 10, 10, 25);
    layout.addView(message);

    android.widget.Button retryButton =
            new android.widget.Button(this);

    retryButton.setText("🔄 Retry");
    retryButton.setTextSize(16);

    retryButton.setOnClickListener(v -> {

        if (isInternetAvailable()) {
            showWebView();
        } else {
            android.widget.Toast.makeText(
                    MainActivity.this,
                    "Internet connection இல்லை",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
        }
    });

    layout.addView(retryButton);

    setContentView(layout);
}
    private void showWebView() {

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(
                new AndroidTTS(),
                "AndroidTTS"
        );

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                installNativeTTSBridge();
            }
        });

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int result =
                                textToSpeech.setLanguage(
                                        Locale.forLanguageTag("en-IN")
                                );

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                            textToSpeech.setLanguage(Locale.US);
                        }

                        textToSpeech.setSpeechRate(0.88f);
                        textToSpeech.setPitch(1.0f);

                        if (android.os.Build.VERSION.SDK_INT >= 21) {

                            textToSpeech.setAudioAttributes(
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

                        ttsReady = true;

                        if (pendingText != null) {

                            String text = pendingText;
                            pendingText = null;

                            speakNative(text);
                        }
                    }
                }
        );

        webView.loadUrl(
                "https://learnenglishthroughtamilfree.blogspot.com/"
        );

        setContentView(webView);
    }

    private void installNativeTTSBridge() {

        String javascript =
                "(function() {" +

                "if (!window.AndroidTTS) return;" +

                "window.lett2StopAudio = function() {" +
                "    AndroidTTS.stop();" +
                "};" +

                "var button = document.getElementById('lett2Listen');" +

                "if (button) {" +

                "    button.onclick = function() {" +

                "        var question = document.getElementById('lett2Question');" +

                "        var text = question ? question.innerText : '';" +

                "        text = text.replace(/_+/g, ' dash ');" +

                "        if (!text.trim()) {" +
                "            AndroidTTS.speak('Please start the quiz first.');" +
                "            return;" +
                "        }" +

                "        button.disabled = true;" +
                "        button.innerHTML = '🔊 Listening...';" +

                "        AndroidTTS.speak(text);" +

                "        setTimeout(function() {" +
                "            button.disabled = false;" +
                "            button.innerHTML = '🔊 Listen';" +
                "        }, 5000);" +

                "    };" +

                "}" +

                "})();";

        webView.evaluateJavascript(
                javascript,
                null
        );
    }

    private void speakNative(String text) {

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        mainHandler.post(() -> {

            if (!ttsReady || textToSpeech == null) {

                pendingText = text;
                return;
            }

            textToSpeech.stop();

            textToSpeech.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "learn_english_tamil"
            );
        });
    }

    private void stopNative() {

        mainHandler.post(() -> {

            if (textToSpeech != null) {
                textToSpeech.stop();
            }
        });
    }

    public class AndroidTTS {

        @JavascriptInterface
        public void speak(String text) {
            speakNative(text);
        }

        @JavascriptInterface
        public void stop() {
            stopNative();
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
}
