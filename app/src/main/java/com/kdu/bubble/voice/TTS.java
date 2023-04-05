package com.kdu.bubble.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

public class TTS implements TextToSpeech.OnInitListener {

    private final Context context;
    private final TextToSpeech tts;

    private final MutableLiveData<Boolean> _isInitialized = new MutableLiveData<>(false);
    public LiveData<Boolean> isInitialized;


    public TTS(Context context) {
        this.context = context.getApplicationContext();
        tts = new TextToSpeech(context, this);

        isInitialized = _isInitialized;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 작업 성공
            int language = tts.setLanguage(Locale.KOREAN);  // 언어 설정
            if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
            } else {
                _isInitialized.setValue(true);
            }
        } else {
            Toast.makeText(context, "TTS 작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void shutdownTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public void speech(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
}
