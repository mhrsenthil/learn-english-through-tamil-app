package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        // Create WebView
        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // Allow media playback
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Connect JavaScript to Android Native TTS
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

        // Initialize Android Text-to-Speech
        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int result = textToSpeech.setLanguage(
                                new Locale("en", "IN")
                        );

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {

                            textToSpeech.setLanguage(
                                    Locale.US
                            );
                        }

                        textToSpeech.setSpeechRate(0.88f);
                        textToSpeech.setPitch(1.0f);

                        ttsReady = true;

                        // Speak text if user pressed Listen
                        // before TTS finished initializing
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

    /**
     * Replace WebView speechSynthesis.speak()
     * with Android native Text-to-Speech.
     */
    private void installNativeTTSBridge() {

        String javascript =
                "(function() {" +

                "if (!window.AndroidTTS) return;" +

                "if (!window.speechSynthesis) return;" +

                "if (window.speechSynthesis.__nativeAndroidTTS) return;" +

                "window.speechSynthesis.__nativeAndroidTTS = true;" +

                "window.__androidCurrentUtterance = null;" +

                "window.__androidTtsFinished = function() {" +

                "  var u = window.__androidCurrentUtterance;" +

                "  window.__androidCurrentUtterance = null;" +

                "  if (u && typeof u.onend === 'function') {" +
                "      u.onend();" +
                "  }" +

                "};" +

                "window.speechSynthesis.speak = function(u) {" +

                "  window.__androidCurrentUtterance = u;" +

                "  AndroidTTS.speak(u.text || '');" +

                "};" +

                "window.speechSynthesis.cancel = function() {" +

                "  AndroidTTS.stop();" +

                "  var u = window.__androidCurrentUtterance;" +
                "  window.__androidCurrentUtterance = null;" +

                "  if (u && typeof u.onend === 'function') {" +
                "      u.onend();" +
                "  }" +

                "};" +

                "})();";

        webView.evaluateJavascript(
                javascript,
                null
        );
    }

    /**
     * Native Android Text-to-Speech
     */
    private void speakNative(String text) {

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        mainHandler.post(() -> {

            if (!ttsReady || textToSpeech == null) {

                pendingText = text;
                return;
            }

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

    /**
     * JavaScript -> Android bridge
     */
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
