package com.learnenglishthroughtamil.free;

import android.app.Activity;
import android.media.AudioAttributes;
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

                            textToSpeech.setLanguage(
                                    Locale.US
                            );
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

                /* Stop browser speech */
                "window.lett2StopAudio = function() {" +
                "    AndroidTTS.stop();" +
                "};" +

                /* Direct Android TTS for Listen button */
                "window.lett2ListenQuestion = function() {" +

                "    try {" +

                "        var q = window.lett2Questions" +
                "            ? window.lett2Questions[window.lett2Current]" +
                "            : null;" +

                "        if (!q) {" +
                "            AndroidTTS.speak('Please start the quiz first.');" +
                "            return;" +
                "        }" +

                "        var text = q.audioText || q.question || '';" +

                "        var button = document.getElementById('lett2Listen');" +

                "        if (button) {" +
                "            button.disabled = true;" +
                "            button.innerHTML = '🔊 Listening...';" +
                "        }" +

                "        AndroidTTS.speak(text);" +

                "        setTimeout(function() {" +
                "            if (button) {" +
                "                button.disabled = false;" +
                "                button.innerHTML = '🔊 Listen';" +
                "            }" +
                "        }, 5000);" +

                "    } catch(e) {" +
                "        AndroidTTS.speak('Audio error.');" +
                "    }" +

                "};" +

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
